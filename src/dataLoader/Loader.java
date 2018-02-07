/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataLoader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author Ivana Clairine
 */
public class Loader {    
    public Map<Integer, Berita> loadDataExcel (String path) throws FileNotFoundException, IOException{
        Map<Integer, Berita> newsList = new HashMap<>();
        File myFile = new File(path); 
        FileInputStream fis = new FileInputStream(myFile);
        XSSFWorkbook myWorkBook = new XSSFWorkbook(fis);
        
        //ambil sheet pertama
        XSSFSheet mySheet = myWorkBook.getSheetAt(0);
        
        for (Row row : mySheet) {
            Berita beritaTemp = new Berita();
            if(row.getRowNum() >= 5) {
                
                Cell idBeritaCell = row.getCell(0);
                beritaTemp.id = (int)idBeritaCell.getNumericCellValue();
                System.out.println(beritaTemp.id);
                Cell labelBeritaCell = row.getCell(1);
                int label = (int)labelBeritaCell.getNumericCellValue();
                //System.out.println("Label: " + label);
                for (Cell cell : row) {
                    int[][] kategori = new int[label][2];
                    int iteratorLabel = 0;
                    
//                    //baca mulai kolom berita
                    if(cell.getColumnIndex() > 1) {
                        switch (cell.getCellType()) {
                            
                            case Cell.CELL_TYPE_STRING:
                                if(!cell.toString().equals("")){
                                    if(beritaTemp.isiberita.equals("")){
                                        beritaTemp.isiberita = cell.toString().toLowerCase().replaceAll("\\d", "");
                                        //System.out.println("Berita: " + beritaTemp.isiberita);
                                    }
                                     
                                    //ambil kategori level 1
                                    CellReference kategoriLv1 = new CellReference(1, cell.getColumnIndex());
                                    Row row1 = mySheet.getRow(kategoriLv1.getRow());
                                    Cell cell1 = row1.getCell(kategoriLv1.getCol());
                                    kategori[iteratorLabel][0] = (int)cell1.getNumericCellValue();
                                    //System.out.println("kategori["+iteratorLabel+"][0]: " + kategori[iteratorLabel][0]);
                                    
                                    //ambil kategori level 2
                                    CellReference kategoriLv2 = new CellReference(3, cell.getColumnIndex());
                                    Row row2 = mySheet.getRow(kategoriLv2.getRow());
                                    Cell cell2 = row2.getCell(kategoriLv2.getCol());
                                    kategori[iteratorLabel][1] = (int)cell2.getNumericCellValue();
                                    //System.out.println("kategori["+iteratorLabel+"][1]: " + kategori[iteratorLabel][1]);
                                    
                                    beritaTemp.kategori[kategori[iteratorLabel][0]][kategori[iteratorLabel][1]] = 1;
                                    beritaTemp.parent.add (kategori[iteratorLabel][0]);
                                    iteratorLabel++;
                                }
                                break;
                            default:
                        }
                    }
                }
            }
            newsList.put(beritaTemp.id, beritaTemp);
        }
        System.out.println("done");
        return newsList;
    }
    
    public void writeDataLevel1(String outputPath, boolean sampling){
        try {
            Map<Integer, Berita> news = loadDataExcel("D:/OneDrive/Semester8/TA2/Daftar Berita/Daftar Berita.xlsx");
            
            for (int i=0; i<10; i++){
//                String outputFile = outputPath + "/dataPositive-" + String.valueOf(i) + ".txt";
//                Writer writer = new BufferedWriter(new FileWriter(new File(outputFile)));
//                 for (Integer key : news.keySet()) {
//                     if (news.get(key).parent.contains(i)) {
//                         writer.write(news.get(key).isiberita + "\n");
//                     }
//                 }
                
                String outputFile = outputPath + "/dataNegative-" + String.valueOf(i) + ".txt";
                Writer writer = new BufferedWriter(new FileWriter(new File(outputFile)));
                ArrayList<Integer> negative = new ArrayList<>();
                for (int j = 0; j<10; j++){
                    if(i != j) {
                        ArrayList<Integer> subNegative = new ArrayList<>();
                        for (Integer key : news.keySet()) {
                            if (!news.get(key).parent.contains(i) && news.get(key).parent.contains(j)) {
                                subNegative.add(key);
                            }
                        }
                        System.out.println("subnegative for class-" + i + " - " + j + ": " + subNegative.size());
                        Random rand = new Random();
                        double negPercentage = (120/9);
                        int negCounter = 0;
                        while (negCounter < negPercentage) {
                            int n = rand.nextInt(subNegative.size());
                            if (!negative.contains(subNegative.get(n))){
                                negCounter++;
                                negative.add(subNegative.get(n));
                            }
                        }
                    }
                }
                
                System.out.println("negative: " + negative.size());
                for (int w=0; w<negative.size(); w++){
                     writer.write(news.get(negative.get(w)).isiberita + "\n");
                }
                 
                writer.flush();
                writer.close();   
            }         
        } catch (IOException ex) {
            Logger.getLogger(Loader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void main(String[] args){
        Loader loader = new Loader();
        loader.writeDataLevel1("C:/Users/Ivana Clairine/Dropbox/S2/TESIS/Data Berita/data190118/", true);
    }
}
