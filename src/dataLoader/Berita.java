/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataLoader;

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
    int[][] kategori;
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
        parent = new ArrayList<>();
    }
}
