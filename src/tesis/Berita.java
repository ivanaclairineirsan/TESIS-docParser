/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tesis;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Map;

/**
 *
 * @author Ivana Clairine
 */
public class Berita {
    int id;
    String judul;
    String isiberita;
    public Map<String, Double> terms;
    int[][] kategori; //deprecated
    ArrayList<Integer> categories; //now we use this. div 10 for lv.1, mod10 for lv.2
    ArrayList<Integer> parent;
    public Berita(int _id, String _judul, String _isiberita, int[][] _kategori){
        id = _id;
        judul = _judul;
        isiberita = _isiberita;
        kategori = _kategori;
        parent = new ArrayList<>();
    }
    
    public Berita(){
        id = 0;
        judul = "";
        isiberita = "";
        kategori = new int[10][4];
        categories = new ArrayList<>();
        parent = new ArrayList<>();
    }
}
