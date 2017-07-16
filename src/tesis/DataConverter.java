/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tesis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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
public class DataConverter {

    public Map<Integer, Berita> newsList;

    public void loadDataExcel(String path) throws FileNotFoundException, IOException {
        newsList = new HashMap<>();
        File myFile = new File(path);
        FileInputStream fis = new FileInputStream(myFile);
        XSSFWorkbook myWorkBook = new XSSFWorkbook(fis);

        //ambil sheet pertama
        XSSFSheet mySheet = myWorkBook.getSheetAt(0);

        for (Row row : mySheet) {
            Berita beritaTemp = new Berita();
            if (row.getRowNum() >= 5) {

                Cell idBeritaCell = row.getCell(0);
                beritaTemp.id = (int) idBeritaCell.getNumericCellValue();
                System.out.println(beritaTemp.id);
                Cell labelBeritaCell = row.getCell(1);
                int label = (int) labelBeritaCell.getNumericCellValue();
                //System.out.println("Label: " + label);
                for (Cell cell : row) {
                    int[][] kategori = new int[label][2];
                    int iteratorLabel = 0;

//                    //baca mulai kolom berita
                    if (cell.getColumnIndex() > 1) {
                        switch (cell.getCellType()) {

                            case Cell.CELL_TYPE_STRING:
                                if (!cell.toString().equals("")) {
                                    if (beritaTemp.isiberita.equals("")) {
                                        beritaTemp.isiberita = cell.toString().toLowerCase().replaceAll("\\d", "");
                                        //System.out.println("Berita: " + beritaTemp.isiberita);
                                    }

                                    //ambil kategori level 1
                                    CellReference kategoriLv1 = new CellReference(1, cell.getColumnIndex());
                                    Row row1 = mySheet.getRow(kategoriLv1.getRow());
                                    Cell cell1 = row1.getCell(kategoriLv1.getCol());
                                    kategori[iteratorLabel][0] = (int) cell1.getNumericCellValue();
//                                    System.out.println("cellValue-level 1: " + cell1.getNumericCellValue());
//                                    System.out.println("kategori["+iteratorLabel+"][0]: " + kategori[iteratorLabel][0]);

                                    //ambil kategori level 2
                                    CellReference kategoriLv2 = new CellReference(3, cell.getColumnIndex());
                                    Row row2 = mySheet.getRow(kategoriLv2.getRow());
                                    Cell cell2 = row2.getCell(kategoriLv2.getCol());
                                    kategori[iteratorLabel][1] = (int) cell2.getNumericCellValue();
//                                    System.out.println("cellValue-level 2: " + cell2.getNumericCellValue());
//                                    System.out.println("kategori["+iteratorLabel+"][1]: " + kategori[iteratorLabel][1]);

                                    beritaTemp.kategori[kategori[iteratorLabel][0]][kategori[iteratorLabel][1]] = 1;
                                    beritaTemp.categories.add(kategori[iteratorLabel][0] * 10 + kategori[iteratorLabel][1]);
                                    beritaTemp.parent.add(kategori[iteratorLabel][0]);
                                    iteratorLabel++;
                                }
                                break;
                            default:
                        }
                    }
                }
            }
            newsList.put(beritaTemp.id, beritaTemp);
//            System.out.println("beritaTemp.id: " + beritaTemp.id);
//            for(int i=0; i<beritaTemp.categories.size(); i++) {
//                System.out.println("kategori-" + i + ": " + getCategoryName(beritaTemp.categories.get(i)));
//            }
        }
        System.out.println("done");
    }

    public void writeDataToTxt(String path, int code) {
        BufferedWriter writer = null;
        try {
            if (code == 1) { //berita     categoryLevel1 categoryLevel2
                writer = new BufferedWriter(new FileWriter(new File(path)));
                for (int i = 0; i < newsList.size(); i++) {
                    writer.write(newsList.get(i).isiberita + "\t");
                    for (int j = 0; j < newsList.get(i).categories.size(); j++) {
                        writer.write(getCategoryName(newsList.get(i).categories.get(j), 2) + ' ');
                    }
                    writer.write("\n");
                }
            } else if (code == 2) { //berita     categoryLevel1
                writer = new BufferedWriter(new FileWriter(new File(path)));
                for (int i = 0; i < newsList.size(); i++) {
                    writer.write(newsList.get(i).isiberita + "\t");
                    for (int j = 0; j < newsList.get(i).categories.size(); j++) {
                        writer.write(getCategoryName(newsList.get(i).categories.get(j), 1) + ' ');
                    }
                    writer.write("\n");
                }
            } else if (code == 3) { //berita      categoryLevel1 (for every category) --> outputing 10 files
                System.out.println("here case 3");
                for (int i = 0; i < 10; i++) {
                    writer = new BufferedWriter(new FileWriter(new File(path + "/Category1-10/data1LevelCategory-" + i + ".txt")));
                    for (int j = 0; j < newsList.size(); j++) {
                        int isWritten = 0;
                        for (int k = 0; k < newsList.get(j).categories.size(); k++) {
                            if (newsList.get(j).categories.get(k) / 10 == i) {
                                isWritten = 1;
                                writer.write(newsList.get(j).isiberita + "\t" + getCategoryName(newsList.get(j).categories.get(k), 1));
                                writer.write("\n");
                            }
                        }
                        if (isWritten == 0 && newsList.get(j).isiberita.length() > 1) {
                            writer.write(newsList.get(j).isiberita + "\t" + "Nope");
                            writer.write("\n");
                        }
                    }
                    writer.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (Exception e) {
            }
        }
    }

    String getCategoryName(int code, int level) {
        int code1 = code / 10;
        int code2 = code % 10;
        String categoryName = "";
        switch (code1) {
            case 0: {
                categoryName = "Pendidikan ";
                if (level > 1) {
                    switch (code2) {
                        case 0:
                            categoryName += "PerguruanTinggi";
                            break;
                        case 1:
                            categoryName += "Balita";
                            break;
                        case 2:
                            categoryName += "Ujian";
                            break;
                        case 3:
                            categoryName += "LainLain";
                            break;
                    }
                }
                break;
            }
            case 1: {
                categoryName = "Politik ";
                if (level > 1) {
                    switch (code2) {
                        case 0:
                            categoryName += "Pemilu";
                            break;
                        case 1:
                            categoryName += "Internasional";
                            break;
                        case 2:
                            categoryName += "Tokoh";
                            break;
                        case 3:
                            categoryName += "LainLain";
                            break;
                    }
                }
                break;
            }
            case 2: {
                categoryName = "HukumKriminal ";
                if (level > 1) {
                    switch (code2) {
                        case 0:
                            categoryName += "Narkoba";
                            break;
                        case 1:
                            categoryName += "KekerasanPencurian";
                            break;
                        case 2:
                            categoryName += "Korupsi";
                            break;
                        case 3:
                            categoryName += "LainLain";
                            break;
                    }
                }
                break;
            }
            case 3: {
                categoryName = "SosialBudaya ";
                if (level > 1) {
                    switch (code2) {
                        case 0:
                            categoryName += "Event";
                            break;
                        case 1:
                            categoryName += "SeniKebudayaan";
                            break;
                        case 2:
                            categoryName += "Agama";
                            break;
                        case 3:
                            categoryName += "LainLain";
                            break;
                    }
                }
                break;
            }
            case 4: {
                categoryName = "Olahraga ";
                if (level > 1) {
                    switch (code2) {
                        case 0:
                            categoryName += "Sepakbola";
                            break;
                        case 1:
                            categoryName += "Badminton";
                            break;
                        case 2:
                            categoryName += "Olimpiade";
                            break;
                        case 3:
                            categoryName += "LainLain";
                            break;
                    }
                }
                break;
            }
            case 5: {
                categoryName = "TeknologiSains ";
                if (level > 1) {
                    switch (code2) {
                        case 0:
                            categoryName += "Sains";
                            break;
                        case 1:
                            categoryName += "Inovasi";
                            break;
                        case 2:
                            categoryName += "GadgetAplikasi";
                            break;
                        case 3:
                            categoryName += "LainLain";
                            break;
                    }
                }
                break;
            }
            case 6: {
                categoryName = "Hiburan ";
                if (level > 1) {
                    switch (code2) {
                        case 0:
                            categoryName += "Selebriti";
                            break;
                        case 1:
                            categoryName += "Film";
                            break;
                        case 2:
                            categoryName += "TravelingKuliner";
                            break;
                        case 3:
                            categoryName += "LainLain";
                            break;
                    }
                }
                break;
            }
            case 7: {
                categoryName = "EkonomiBisnis ";
                if (level > 1) {
                    switch (code2) {
                        case 0:
                            categoryName += "Investasi";
                            break;
                        case 1:
                            categoryName += "Ekonomi";
                            break;
                        case 2:
                            categoryName += "Internasional";
                            break;
                        case 3:
                            categoryName += "LainLain";
                            break;
                    }
                }
                break;
            }
            case 8: {
                categoryName = "BencanaKecelakaan ";
                if (level > 1) {
                    switch (code2) {
                        case 0:
                            categoryName += "KecelakaanTransportasi";
                            break;
                        case 1:
                            categoryName += "BencanaAlam";
                            break;
                        case 2:
                            categoryName += "Wabah";
                            break;
                        case 3:
                            categoryName += "LainLain";
                            break;
                    }
                }
                break;
            }
            case 9: {
                categoryName = "Kesehatan ";
                if (level > 1) {
                    switch (code2) {
                        case 0:
                            categoryName += "Virus";
                            break;
                        case 1:
                            categoryName += "Fasilitas";
                            break;
                        case 2:
                            categoryName += "Obat";
                            break;
                        case 3:
                            categoryName += "LainLain";
                            break;
                    }
                }
                break;
            }
        }
        return categoryName;
    }

    public static void main(String[] args) {
        DataConverter dc = new DataConverter();
        try {
            dc.loadDataExcel("D:/ITB/S2/TESIS/seq2seq/Daftar Berita/Daftar Berita.xlsx");
            dc.writeDataToTxt("D:/ITB/S2/TESIS/seq2seq/Data Berita/data2Level.txt", 1);
            dc.writeDataToTxt("D:/ITB/S2/TESIS/seq2seq/Data Berita/data1Level.txt", 2);
            dc.writeDataToTxt("D:/ITB/S2/TESIS/seq2seq/Data Berita", 3);

        } catch (IOException ex) {
            Logger.getLogger(DataConverter.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }
}
