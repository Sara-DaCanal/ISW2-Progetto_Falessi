package weka_analysis;

import com.opencsv.CSVReader;

import java.io.FileReader;
import java.io.IOException;

public class AnalysisCSV {
    private String dataset;
    private int n_training;
    private int p_training;
    private int defect_training;
    private int defect_testing;
    private String classifier;
    private String balancing;
    private String featureSelection;
    private String sensitivity;
    private double truePositive;
    private double falsePositive;
    private double trueNegative;
    private double falseNegative;
    private double precision;
    private double recall;
    private double AUC;
    private double kappa;

    public AnalysisCSV(String dataset, int index, String training, String testing) throws IOException {
        this.dataset=dataset;
        this.n_training=index;
        int[] t1 = calculate(training);
        int [] t2 = calculate(testing);
        this.p_training = (t1[0]*100/(t1[0]+t2[0]));
        this.defect_training = (t1[1]*100/t1[0]);
        this.defect_testing = (t2[1]*100/t2[0]);
    }
    private AnalysisCSV(String dataset, int index, int p_training, int defect_training, int defect_testing, String classifier, String balancing, String featureSelection, String sensitivity, double[] rates, double precision, double recall, double AUC, double kappa){
        this.dataset=dataset;
        this.n_training=index;
        this.p_training=p_training;
        this.defect_training=defect_training;
        this.defect_testing=defect_testing;
        this.classifier= classifier;
        this.balancing = balancing;
        this.featureSelection=featureSelection;
        this.sensitivity = sensitivity;
        this.truePositive = rates[0];
        this.falsePositive = rates[1];
        this.trueNegative = rates[2];
        this.falseNegative = rates[3];
        this.precision=precision;
        this.recall=recall;
        this.AUC=AUC;
        this.kappa=kappa;
    }

    private int[] calculate(String path) throws IOException {
        int[] results = {0,0};
        try(CSVReader csvReader = new CSVReader(new FileReader(path))){
            String[] line;
            while((line = csvReader.readNext())!=null){
                results[0]++;
                if(line[line.length-1].equals("true")) results[1]++;
            }
        }
        return results;
    }

    private double[] ratesArray(){
        return new double[]{this.truePositive, this.falsePositive, this.trueNegative, this.falseNegative};
    }

    public static AnalysisCSV copyOf(AnalysisCSV oldACSV) throws IOException {
        return new AnalysisCSV(oldACSV.dataset, oldACSV.n_training, oldACSV.p_training, oldACSV.defect_training, oldACSV.defect_testing, oldACSV.classifier, oldACSV.balancing, oldACSV.featureSelection, oldACSV.sensitivity, oldACSV.ratesArray(), oldACSV.precision, oldACSV.recall, oldACSV.AUC, oldACSV.kappa);
    }

    public void setClassifier(String classifier) {
        this.classifier = classifier;
    }

    public void setBalancing(String balancing){
        this.balancing=balancing;
    }

    public void setFeatureSelection(String featureSelection){
        this.featureSelection= featureSelection;
    }

    public void setSensitivity(String sensitivity){
        this.sensitivity=sensitivity;
    }

    public void setRates(double tp, double fp, double tn, double fn){
        this.truePositive = tp;
        this.falsePositive = fp;
        this.trueNegative = tn;
        this.falseNegative =fn;
    }
    public void setMetrics(double precision, double recall, double AUC, double kappa){
        this.kappa=kappa;
        this.AUC=AUC;
        this.recall=recall;
        this.precision=precision;
    }

    public String[] toStringArray(){
        return new String[]{
                this.dataset,
                Integer.toString(n_training),
                Integer.toString(p_training),
                Integer.toString(defect_training),
                Integer.toString(defect_testing),
                this.classifier,
                this.balancing,
                this.featureSelection,
                this.sensitivity,
                Double.toString(Math.round(truePositive*1000000)/1000000d),
                Double.toString(Math.round(falsePositive*1000000)/1000000d),
                Double.toString(Math.round(trueNegative*1000000)/1000000d),
                Double.toString(Math.round(falseNegative*1000000)/1000000d),
                Double.toString(Math.round(precision*1000000)/1000000d),
                Double.toString(Math.round(recall*1000000)/1000000d),
                Double.toString(Math.round(AUC*1000000)/1000000d),
                Double.toString(Math.round(kappa*1000000)/1000000d)};
    }
}


