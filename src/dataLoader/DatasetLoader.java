package dataLoader;

import database.DatabaseHelper;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Ivana Clairine
 */
public class DatasetLoader {
    
    public void getNewsWithClass(int class_id) {
        try {
            DatabaseHelper.Connect();
            ResultSet results = DatabaseHelper.executeQuery("SELECT * FROM artikel_kategori join artikel on artikel_kategori.ID_ARTIKEL = artikel.ID_ARTIKEL where id_kelas=" + String.valueOf(class_id));
            while (results.next()) {
                String insertDataset = "INSERT INTO dataset_tesis_ivana (id, judul, full_text, id_kelas) values (";
                insertDataset = insertDataset + results.getString("ID_ARTIKEL") + ", \"";
                insertDataset = insertDataset + results.getString("JUDUL").replaceAll("[^a-zA-Z0-9 ]+"," ").replaceAll("( )+", " ") + "\", \"";
                insertDataset = insertDataset + results.getString("FULL_TEXT").replaceAll("[^a-zA-Z0-9 ]+"," ").replaceAll("( )+", " ") + "\", ";
                insertDataset = insertDataset + results.getString("ID_KELAS") + ");";
                
                System.out.println(insertDataset);
                DatabaseHelper.execute(insertDataset);
            }
            DatabaseHelper.Disconnect();
        } catch (SQLException ex) {
            DatabaseHelper.Disconnect();
        }
    }
    
    public ArrayList<Berita> getNewsFromDataset (int class_id, int size) {
          try {
            DatabaseHelper.Connect();
            String getQuery = "SELECT * FROM DATASET_TESIS_IVANA where is_annotated=0 and id_kelas=" + String.valueOf(class_id) + 
                              " and id not in (select id from dataset_tesis_annotated where kelas_" + String.valueOf(class_id) + "=1 )" +
                              " order by id desc limit " + String.valueOf(size); 
            System.out.println(getQuery);
            ResultSet results = DatabaseHelper.executeQuery(getQuery);
            ArrayList<Berita> news = new ArrayList<>();
            while (results.next()) {
                Berita temp = new Berita();
                temp.id=results.getInt("id");
                temp.isiberita=results.getString("full_text");
                temp.judul=results.getString("judul");
                news.add(temp);
            }
            DatabaseHelper.Disconnect();
            return news;
        } catch (SQLException ex) {
            DatabaseHelper.Disconnect();
            return null;
        }
    }
    
    public void doAnnotateMultilabel(int class_id, int size, String filename) throws IOException, SQLException {
        ArrayList<Berita> unannotatedNews = getNewsFromDataset(class_id, size);
        Scanner scanner = new Scanner( System.in );
        ArrayList<Berita> positive = new ArrayList<>();
        ArrayList<Berita> negative = new ArrayList<>();
        ArrayList<Berita> undetermined = new ArrayList<>();
        HashMap<Integer, ArrayList<Berita>> additionalClass = new HashMap<>();
        
        for(int i=0; i<unannotatedNews.size(); i++){
            System.out.println(String.valueOf(i) + " - Judul: " + unannotatedNews.get(i).judul);
            System.out.print("Data positif? (y/n): ");
            String s = scanner.nextLine();
            if (s.charAt(0) == 'y') {
                positive.add(unannotatedNews.get(i));
            } else if (s.charAt(0) == 'n') {
                negative.add(unannotatedNews.get(i));
            } else if (s.charAt(0) == 'z') {
                i = i-2;
                if (positive.contains(unannotatedNews.get(i+1))) {
                    positive.remove(unannotatedNews.get(i+1));
                } else if (negative.contains(unannotatedNews.get(i+1))){
                    negative.remove(unannotatedNews.get(i+1));
                } else {
                    undetermined.remove(unannotatedNews.get(i+1));
                }
            } else if (s.charAt(0) == 'u') {
                undetermined.add(unannotatedNews.get(i));
            }
            
            System.out.print("Tambahan kelas [1:Pendidikan 2:Politik: 3:Hukum&Kriminal 4:Sosial&Budaya "
                    + "5:Olahraga 6:Tech-Science 7:Hiburan 8:Ekonomi 9:Kesehatan 10: Bencana]: ");
            String classes_added = scanner.nextLine().replace("\n", "");
            System.out.println(classes_added);
            if (!(classes_added.equals("0") || classes_added.equals("n"))) {
                List<String> classes = new ArrayList<String>(Arrays.asList(classes_added.split(",")));
                for (String cls : classes) {
                    if(additionalClass.containsKey(cls)) {
                        ArrayList<Berita> temp = additionalClass.get(cls);
                        temp.add(unannotatedNews.get(i));
                        additionalClass.put(Integer.valueOf(cls), temp);
                    } else {
                        ArrayList<Berita> temp = new ArrayList<>();
                        temp.add(unannotatedNews.get(i));
                        additionalClass.put(Integer.valueOf(cls), temp);
                    }
                }
            }
        }
        
        writeDatasetToFile(class_id, filename, positive, negative, additionalClass);
        updateDatasetAnnotated(class_id, positive, negative, undetermined, additionalClass);
    }
    
    public String getClassName (int class_id){
        switch(class_id) {
            case 1: return "Pendidikan";
            case 2: return "Politik";
            case 3: return "Hukum & Kriminal";
            case 4: return "Sosial budaya";
            case 5: return "Olahraga";
            case 6: return "Teknologi & Sains";
            case 7: return "Hiburan";
            case 8: return "Bisnis & Ekonomi";
            case 9: return "Kesehatan";
            case 10: return "Bencana & Kecelakaan";
        }
        return null;
    }
    
    public void doAnnotate(int class_id, int size, String filename) throws IOException {
        ArrayList<Berita> unannotatedNews = getNewsFromDataset (class_id, size);
        Scanner scanner = new Scanner( System.in );
        FileWriter fwPos = new FileWriter(filename + class_id + "-pos.txt",true);
        FileWriter fwNeg = new FileWriter(filename + class_id + "-neg.txt",true);
        ArrayList<Berita> positive = new ArrayList<>();
        ArrayList<Berita> negative = new ArrayList<>();
        ArrayList<Berita> undetermined = new ArrayList<>();
        
        for(int i=0; i<unannotatedNews.size(); i++){
            System.out.println(String.valueOf(i) + " - Judul: " + unannotatedNews.get(i).judul);
            System.out.print("Data positif? (y/n): ");
            String s = scanner.nextLine();
            if (s.charAt(0) == 'y') {
                positive.add(unannotatedNews.get(i));
            } else if (s.charAt(0) == 'n') {
                negative.add(unannotatedNews.get(i));
            } else if (s.charAt(0) == 'z') {
                i = i-2;
                if (positive.contains(unannotatedNews.get(i+1))) {
                    positive.remove(unannotatedNews.get(i+1));
                } else if (negative.contains(unannotatedNews.get(i+1))){
                    negative.remove(unannotatedNews.get(i+1));
                } else {
                    undetermined.remove(unannotatedNews.get(i+1));
                }
            } else if (s.charAt(0) == 'u') {
                undetermined.add(unannotatedNews.get(i));
            }
        }
        
        System.out.println("Positive: " + positive.size());
        System.out.println("Negative: " + negative.size());
        
        for (Berita pos : positive) {
            fwPos.write(pos.isiberita);
            fwPos.write("\n");
        }
        
        for (Berita neg : negative) {
            fwNeg.write(neg.isiberita);
            fwNeg.write("\n");
        }
        fwPos.close();
        fwNeg.close();
        
        updateDatasetAnnotated(positive, negative, undetermined);
    }

    private void updateDatasetAnnotated(ArrayList<Berita> positive, ArrayList<Berita> negative, ArrayList<Berita> undetermined) {
        DatabaseHelper.Connect();
        System.out.println("update positive");
        for (Berita pos: positive) {
            String updateQuery = "UPDATE dataset_tesis_ivana set is_annotated=1 where id=" + String.valueOf(pos.id);
            DatabaseHelper.execute(updateQuery);
        }
        System.out.println("update negative");
        for (Berita neg: negative) {
            String updateQuery = "UPDATE dataset_tesis_ivana set is_annotated=2 where id=" + String.valueOf(neg.id);
            DatabaseHelper.execute(updateQuery);
        }
        System.out.println("update undetermined");
        for (Berita und: undetermined) {
            String updateQuery = "UPDATE dataset_tesis_ivana set is_annotated=3 where id=" + String.valueOf(und.id);
            DatabaseHelper.execute(updateQuery);
        }
        DatabaseHelper.Disconnect();
    }
    
    private void updateDatasetAnnotated(int class_id, ArrayList<Berita> positive, ArrayList<Berita> negative, ArrayList<Berita> undetermined, HashMap<Integer, ArrayList<Berita>> addedClass) throws SQLException {
        DatabaseHelper.Connect();
        System.out.println("update positive");
        for (Berita pos: positive) {
            String updateQuery = "UPDATE dataset_tesis_ivana set is_annotated=1 where id=" + String.valueOf(pos.id);
            DatabaseHelper.execute(updateQuery);
            String selectQuery = "SELECT * from dataset_tesis_annotated where id=" + String.valueOf(pos.id);
            ResultSet results = DatabaseHelper.executeQuery(selectQuery);
            if (!results.next() ) {
                String insertQuery = "INSERT INTO dataset_tesis_annotated (id, judul, kelas_" + class_id + ") "
                        + "values (" + String.valueOf(pos.id) + ", \"" + pos.judul + "\", 1)";
                System.out.println(insertQuery);
                DatabaseHelper.execute(insertQuery);
            } else {
                while(results.next()){ 
                    updateQuery = "UPDATE dataset_tesis_annotated set kelas_" + class_id + "=1 where id=" + String.valueOf(pos.id);
                    DatabaseHelper.execute(updateQuery);
                }
            }
        }
        System.out.println("update negative");
        for (Berita neg: negative) {
            String updateQuery = "UPDATE dataset_tesis_ivana set is_annotated=2 where id=" + String.valueOf(neg.id);
            DatabaseHelper.execute(updateQuery);
            String selectQuery = "SELECT * from dataset_tesis_annotated where id=" + String.valueOf(neg.id);
            ResultSet results = DatabaseHelper.executeQuery(selectQuery);
            if (!results.next() ) {
                String insertQuery = "INSERT INTO dataset_tesis_annotated (id, judul, kelas_" + class_id + ") "
                        + "values (" + String.valueOf(neg.id) + ", \"" + neg.judul + "\", 0)";
                DatabaseHelper.execute(insertQuery);
            } else {
                while(results.next()){ 
                    updateQuery = "UPDATE dataset_tesis_annotated set kelas_" + class_id + "=0 where id=" + String.valueOf(neg.id);
                    DatabaseHelper.execute(updateQuery);
                }
            }
        }
        System.out.println("update undetermined");
        for (Berita und: undetermined) {
            String updateQuery = "UPDATE dataset_tesis_ivana set is_annotated=3 where id=" + String.valueOf(und.id);
            DatabaseHelper.execute(updateQuery);
        }
        System.out.println("update added class");
        System.out.println("Added_class size: " + addedClass.toString());
        for(Integer cls : addedClass.keySet()) {
            ArrayList<Berita> news = addedClass.get(cls);
            System.out.println("news.size: " + news.size());
            for (Berita b: news) {
                String selectQuery = "SELECT * from dataset_tesis_annotated where id=" + String.valueOf(b.id);
                ResultSet results = DatabaseHelper.executeQuery(selectQuery);
                if (!results.next() ) {
                String insertQuery = "INSERT INTO dataset_tesis_annotated (id, judul, kelas_" + cls + ") "
                        + "values (" + String.valueOf(b.id) + ", \"" + b.judul + "\", 1)";
                    System.out.println(insertQuery);
                DatabaseHelper.execute(insertQuery);
                } else {
                    do{ 
                        String updateQuery = "UPDATE dataset_tesis_annotated set kelas_" + String.valueOf(cls) + "=1 where id=" + String.valueOf(b.id);
                        System.out.println(updateQuery);
                        DatabaseHelper.execute(updateQuery);
                    }while(results.next());
                }
            }
        }
        DatabaseHelper.Disconnect();
    }
    
    public ArrayList<Berita> getNegativeClassFromDataset (int class_id, int size) {
          try {
            DatabaseHelper.Connect();
            ArrayList<Berita> news = new ArrayList<>();
            
            for(int i=1; i<11; i++){
                if(i != class_id) {
                    ArrayList<Berita> tempNeg = new ArrayList<>();
                    String getQuery = "SELECT * FROM DATASET_TESIS_IVANA where id_kelas=" + String.valueOf(i) + 
                                      " and id not in (select id from dataset_tesis_ivana where id_kelas=" + String.valueOf(class_id) + 
                                      ") limit " + String.valueOf(size);
                    System.out.println(getQuery);
                    ResultSet results = DatabaseHelper.executeQuery(getQuery);
                    while (results.next()) {
                        Berita temp = new Berita();
                        temp.id=results.getInt("id");
                        temp.isiberita=results.getString("full_text");
                        temp.judul=results.getString("judul");
                        tempNeg.add(temp);
                    }
                    for(int j=0; j<size/9; j++) {
                        Random rand = new Random();
                        int n = rand.nextInt(size-1) + 0;
                        news.add(tempNeg.get(n));
                    }
                }
            }
            
            DatabaseHelper.Disconnect();
            return news;
        } catch (SQLException ex) {
            DatabaseHelper.Disconnect();
            return null;
        }
    }
    
    private void writeNegativeFile(int class_id, int size, String filename) throws IOException {
        ArrayList<Berita> negative = getNegativeClassFromDataset(class_id, size);
        FileWriter fwNeg = new FileWriter(filename + class_id + "-neg.txt", true);
        for (Berita neg : negative) {
            fwNeg.write(neg.isiberita);
            fwNeg.write("\n");
        }
        fwNeg.close();
    }
    
    private void updateDatabase (String filePos, String fileNeg) throws SQLException {
         try {
            DatabaseHelper.Connect();
            File f = new File(filePos);
            BufferedReader b = new BufferedReader(new FileReader(f));
            String readLine = "";
            System.out.println("Update positive data");
            while ((readLine = b.readLine()) != null) {
                String selectQuery = "SELECT id, judul FROM dataset_tesis_ivana where FULL_TEXT like \"" + readLine + "\" limit 1";
                ResultSet results = DatabaseHelper.executeQuery(selectQuery);
                while (results.next()) {
                    String insertQuery = "INSERT into dataset_tesis_annotated (id, judul, kelas_1) values (" + 
                            results.getInt("id") + ", \"" + results.getString("judul") + "\", 1);";
                    System.out.println(insertQuery);
                    DatabaseHelper.execute(insertQuery);
                }
            }
            System.out.println("Update negative data");
            f = new File(fileNeg);
            b = new BufferedReader(new FileReader(f));
            readLine = "";
            while ((readLine = b.readLine()) != null) {
                String selectQuery = "SELECT id, judul FROM dataset_tesis_ivana where FULL_TEXT like \"" + readLine + "\" limit 1";
                ResultSet results = DatabaseHelper.executeQuery(selectQuery);
                while (results.next()) {
                    String insertQuery = "INSERT into dataset_tesis_annotated (id, judul, kelas_1) values (" + 
                            results.getInt("id") + ", \"" + results.getString("judul") + "\", 0);";
                    System.out.println(insertQuery);
                    DatabaseHelper.execute(insertQuery);
                }
//                String updateQuery = "UPDATE dataset_tesis_ivana set is_annotated=2 where id_kelas=1 and full_text like \"" + readLine + "\"";
//                System.out.println(updateQuery);
//                DatabaseHelper.execute(updateQuery);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void writeDatasetToFile(int class_id, String filename, ArrayList<Berita> positive, ArrayList<Berita> negative, HashMap<Integer, ArrayList<Berita>> additionalClass) throws IOException {
        FileWriter fwPos = new FileWriter(filename + class_id + "-pos.txt",true);
        FileWriter fwNeg = new FileWriter(filename + class_id + "-neg.txt",true);
        
        for (Berita pos : positive) {
            fwPos.write(pos.isiberita);
            fwPos.write("\n");
        }
        fwPos.close();
        
        for (Berita neg : negative) {
            fwNeg.write(neg.isiberita);
            fwNeg.write("\n");
        }
        fwNeg.close();
        
        for (Integer key: additionalClass.keySet()) {
            FileWriter fwPos2 = new FileWriter(filename + String.valueOf(key) + "-pos.txt",true);
            ArrayList<Berita> tempNews = additionalClass.get(key);
            
            for (Berita b: tempNews) {
                fwPos2.write(b.isiberita);
                fwPos2.write("\n");
            }
            fwPos2.close();
        }
    }
    
     public static void main(String[] args) throws IOException, SQLException {
        DatasetLoader loader = new DatasetLoader();
//        for(int i=1; i<11; i++)
//            loader.getNewsWithClass(i);
        
//        System.out.println("--- PENDIDIKAN ---");
//        loader.doAnnotate(1, 100, "C:/Users/Ivana Clairine/Dropbox/S2/TESIS/Data Berita/data040218/dataAnnotate-");
        
//        loader.updateDatabase("C:/Users/Ivana Clairine/Dropbox/S2/TESIS/Data Berita/data040218/dataAnnotate-1-pos.txt", "C:/Users/Ivana Clairine/Dropbox/S2/TESIS/Data Berita/data040218/dataAnnotate-1-neg.txt");
        
//        loader.writeNegativeFile(1, 68,  "C:/Users/Ivana Clairine/Dropbox/S2/TESIS/Data Berita/data040218/dataAnnotate-");
        
        loader.doAnnotateMultilabel(1, 100,  "C:/Users/Ivana Clairine/Dropbox/S2/TESIS/Data Berita/data070218/dataAnnotate-");
    }

}
