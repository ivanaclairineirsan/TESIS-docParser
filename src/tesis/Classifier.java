
import IndonesianNLP.IndonesianSentenceTokenizer;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import static java.util.Arrays.asList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import mulan.classifier.MultiLabelOutput;
import mulan.classifier.transformation.CalibratedLabelRanking;
import org.jscience.mathematics.number.Float64;
import org.jscience.mathematics.vector.DenseVector;
import org.nlp.vec.VectorModel;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.FastVector;
import weka.core.Instances;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author dosen
 */
public class Classifier {
    
    Instances newDataset;
    HashSet<String> stopWords;
    IndonesianSentenceTokenizer tokenizer;
    VectorModel vm; //w2v model
    CalibratedLabelRanking clr; //clr-svm model

    static final int numClass = 10;
    static final int numVector = 500;
    static final String stopWordsFileName = "txt/StopWordList.txt";
    static final String w2vModFileName = "word2vec/mod.nn";
    static final String svmModFileName = "txt/CLR_SVM.mod";
    static final List<String> className = asList("class-Pendidikan", "class-Politik", "class-HukumKriminal", "class-SosialBudaya", "class-Olahraga", "class-TeknologiSains", "class-Hiburan", "class-EkonomiBisnis", "class-Kesehatan", "class-BencanaKecelakaan");
        
    public Classifier() {
        ObjectInputStream objectInputStream = null;
        try {
            //tokenizer
            tokenizer = new IndonesianSentenceTokenizer();
            //load w2v model
            vm = VectorModel.loadFromFile(w2vModFileName);
            int vocabSize = vm.getWordMap().size();
            System.out.println("vocab size: " + vocabSize);
            //load clr-svm model
//            objectInputStream = new ObjectInputStream(new BufferedInputStream(new FileInputStream(svmModFileName)));
//            clr = (CalibratedLabelRanking) objectInputStream.readObject();
//            objectInputStream.close();
            //read stopwords
            stopWords = new HashSet<>();
//            try (BufferedReader br = new BufferedReader(new FileReader(stopWordsFileName))) {
//                String sCurrentLine;
//                while ((sCurrentLine = br.readLine()) != null) {
//                    stopWords.add(sCurrentLine.toLowerCase());
//                }
//            } catch (IOException e) {
//                System.err.println(e.getMessage());
//            }
//        } catch (FileNotFoundException ex) {
//            Logger.getLogger(Classifier.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IOException | ClassNotFoundException ex) {
//            Logger.getLogger(Classifier.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
//            try {
//                objectInputStream.close();
//            } catch (IOException ex) {
//                Logger.getLogger(Classifier.class.getName()).log(Level.SEVERE, null, ex);
//            }
        }
    }
    
    public void classify(String news) {
        try {
            /* CREATE INSTANCE*/
            FastVector atts;
            FastVector attVals;
            double[] vals;

            // 1. set up attributes
            atts = new FastVector();
            // - numeric for vector element
            for (int i = 0; i < numVector; i++) {
                atts.addElement(new Attribute("att" + i));
            }
            // - nominal for classes
            attVals = new FastVector();
            attVals.addElement("0");
            attVals.addElement("1");
            for (String cn : className) {
                atts.addElement(new Attribute(cn.replace("class-", ""), attVals));
            }

            // 2. create Instances object
            newDataset = new Instances("MyRelation", atts, 0);

            // 3. fill with new news
            vals = new double[newDataset.numAttributes()];

            //initiate sum vector
            Float64[] sumArr = new Float64[numVector];
            for (int i = 0; i < numVector; i++) {
                sumArr[i] = Float64.ZERO;
            }
            DenseVector<Float64> sum = DenseVector.valueOf(sumArr);

            //calculate sum per token
            ArrayList<String> tokens = tokenize(news);
            int tokensSize = tokens.size();
            for (String token : tokens) {
                //no stemming but swElimination
                if (stopWords.contains(token.toLowerCase())) {   //SW eliminatiion
                    tokensSize--;
                } else {
                    sum = sum.plus(word2vec(token.toLowerCase()));
                }
            }

            //calculate average from sum
            sum = sum.times(Float64.ONE.divide(tokensSize));
            
            for (int j = 0; j < numVector; j++) {
                vals[j] = sum.get(j).doubleValue();
            }
            //init all classes w/ 0
            for (int j = numVector; j < numVector + numClass; j++) {
                vals[j] = 0;
            }
            newDataset.add(new DenseInstance(1.0, vals));

//            /* CLASSIFY INSTANCE*/
//            weka.core.Instance ins = newDataset.instance(0);
//            MultiLabelOutput output = clr.makePrediction(ins);
//            // do necessary operations with provided prediction output, here just print it out
//            System.out.println(output);
        } catch (Exception ex) {
            Logger.getLogger(Classifier.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public ArrayList<String> tokenize(String txt) {
        ArrayList<String> tokens = tokenizer.tokenizeSentence(txt.toLowerCase());
        return tokens;
    }
    
    public DenseVector word2vec(String word) {
        float[] vecArr = vm.getWordVector(word);
        Float64[] vecArr64 = new Float64[numVector];
        if (vecArr != null) {
            for (int i = 0; i < vecArr.length; i++) {
                vecArr64[i] = Float64.valueOf(vecArr[i]);
            }
        } else {
            for (int i = 0; i < numVector; i++) {
                vecArr64[i] = Float64.ZERO;
            }
        }
        DenseVector<Float64> vec = DenseVector.valueOf(vecArr64);
        return vec;
    }
    
    public static void main(String args[]) {
        Classifier classifier = new Classifier();
        classifier.classify("REPUBLIKA.CO.ID,JAKARTA--PT XL Axiata Tbk (XL) selama kuartal I 2013 membukukan pendapatan sebesar Rp 5,047 triliun, tumbuh 2 persen dibanding pendapatan periode sama 2012 sebesar Rp4,925 triliun.\n"
                + "\"Kenaikan pendapatan didorong tumbuhnya layanan data yang melonjak hingga 16 persen,\" kata Presiden Direktur XL Axiata Hasnul Suhaimi, dalam siaran pers di Jakarta, Rabu.\n"
                + "Menurut Hasnul, pencapaian kinerja XL selama kuartal I dipengaruhi penawaran paket-paket layanan dengan harga yang lebih terjangkau (affordable), untuk mendorong peningkatan jumlah pelanggan.\n"
                + "Faktor lainnya yang turut mempengaruhi adalah pembangunan infrastruktur BTS untuk menambah kapasitas layanan, yang banyak dilakukan oleh XL selama periode sebelumnya, sehingga penyerapan beban biaya penyewaan tower mulai ditanggung sejak periode kuartal pertama ini.\n"
                + "Hal lainnya adalah adanya beban rugi kurs yang harus ditanggung XL sebesar Rp26 milliar, sehingga pada akhirnya laba XL hanya sebesar Rp316 milliar atau turun dari sebelumnya Rp667 miliar.\n"
                + "\"Di tengah persaingan industri yang kian ketat, selama kuartal I 2013 sebagaimana tahun?tahun sebelumnya, XL mengawalinya dengan pencapaian yang agak berat, namun selalu berhasil di kuarta-kuartal berikutnya,\" tegas Hasnul.\n"
                + "Secara keseluruhan, pertumbuhan kontribusi layanan data mencerminkan arah bisnis serta adanya peluang yang besar yang dapat dimanfaatkan XL untuk dapat terus meraih potensi pertumbuhan di bisnis layanan data di masa yang akan datang.\n"
                + "Penggunaan layanan data di Indonesia terus meningkat dari waktu ke waktu, termasuk di XL, yang mendorong terjadinya peningkatan traffic sebesar 40 persen.\n"
                + "Dengan jumlah pelanggan data sebesar 29,1 juta atau mendekati 60 persen dari total jumlah pelanggan XL, mendorong peningkatan kontribusi pendapatan layanan data menjadi sebesar 22 persen dibandingkan dengan tahun lalu sebesar 19 persen.\n"
                + "\"XL selalu fokus dalam mengembangkan akses dan layanan, serta fleksibel dalam memenuhi tuntutan pasar. Selain mempercepat pembangunan infrastruktur data untuk memenuhi permintaan layanan Data yang terus tumbuh, kami juga meningkatkan pemanfaatan aset melalui manajemen kapasitas yang lebih baik,\" ujarnya.\n"
                + "Adapun total jumlah pelanggan XL hingga kuartal I 2013 mencapai 49,1 juta naik sekitar 6 persen, dari periode sama 2012 sebesar 46,4 juta pelanggan.\n"
                + "Fokus XL tahun ini lebih meningkatkan pemanfaatan jaringan XL, sekaligus memprioritaskan perluasan infrastruktur data.");
    }
}
