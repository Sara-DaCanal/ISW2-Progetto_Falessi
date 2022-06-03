package weka_analysis;

import com.opencsv.CSVReader;

import java.io.FileReader;
import java.io.IOException;

public class AnalysisCSV {
    private String dataset;
    private int nTraining;
    private int pTraining;
    private int defectTraining;
    private int defectTesting;
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
    private double auc;
    private double kappa;

    public AnalysisCSV(String dataset, int index, String training, String testing) throws IOException {
        this.dataset=dataset;
        this.nTraining=index;
        int[] t1 = calculate(training);
        int [] t2 = calculate(testing);
        this.pTraining = (t1[0]*100/(t1[0]+t2[0]));
        this.defectTraining = (t1[1]*100/t1[0]);
        this.defectTesting = (t2[1]*100/t2[0]);
    }
    private AnalysisCSV(String dataset, int[] percentages, String classifier, String balancing, String featureSelection, String sensitivity, double[] rates, double[]metrics){
        this.dataset=dataset;
        this.nTraining=percentages[0];
        this.pTraining=percentages[1];
        this.defectTraining=percentages[2];
        this.defectTesting=percentages[3];
        this.classifier= classifier;
        this.balancing = balancing;
        this.featureSelection=featureSelection;
        this.sensitivity = sensitivity;
        this.truePositive = rates[0];
        this.falsePositive = rates[1];
        this.trueNegative = rates[2];
        this.falseNegative = rates[3];
        this.precision=metrics[0];
        this.recall=metrics[1];
        this.auc=metrics[2];
        this.kappa=metrics[3];
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
    private int[] percentagesArray(){
        return new int[]{this.nTraining, this.pTraining, this.defectTraining, this.defectTesting};
    }
    private double[] ratesArray(){
        return new double[]{this.truePositive, this.falsePositive, this.trueNegative, this.falseNegative};
    }
    private double[] metricsArray(){
        return new double[]{this.precision, this.recall, this.auc, this.kappa};
    }

    public static AnalysisCSV copyOf(AnalysisCSV oldACSV) {
        return new AnalysisCSV(oldACSV.dataset, oldACSV.percentagesArray(), oldACSV.classifier, oldACSV.balancing, oldACSV.featureSelection, oldACSV.sensitivity, oldACSV.ratesArray(), oldACSV.metricsArray());
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
    public void setMetrics(double precision, double recall, double auc, double kappa){
        this.kappa=kappa;
        this.auc=auc;
        this.recall=recall;
        this.precision=precision;
    }

    public String[] toStringArray(){
        return new String[]{
                this.dataset,
                Integer.toString(nTraining),
                Integer.toString(pTraining),
                Integer.toString(defectTraining),
                Integer.toString(defectTesting),
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
                Double.toString(Math.round(auc*1000000)/1000000d),
                Double.toString(Math.round(kappa*1000000)/1000000d)};
    }
}


