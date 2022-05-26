package weka_analysis;

import com.opencsv.CSVWriter;
import csvfile.CSVList;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WekaParameters {

    private String projectName;
    private int releaseNumber=1;
    private List<CSVList> map;
    private double[] randomForest ={0.0,0.0,0.0,0.0};
    private double[] naiveBayes ={0.0,0.0,0.0,0.0};
    private double[] ibk = {0.0,0.0,0.0,0.0};
    private List<List<String>> results;

    public WekaParameters(String projectName, List<CSVList> map){
        this.projectName=projectName;
        this.releaseNumber=map.size()/2;
        this.map=map;
    }

    public void wekaAnalysis() throws Exception {

        results = new ArrayList<>();

        for (int i = 0; i < releaseNumber - 1; i++) {

            String testPath = TestingSet.getInstance(projectName.toLowerCase(Locale.ROOT), map).testingSetCreator();

            String testArffFile = new CSV2Arff(testPath, map).converter();

            String trainingPath = "./training_" + projectName.toLowerCase(Locale.ROOT) + "_"+(i + 1) + ".csv";

            String trainingArffFile = new CSV2Arff(trainingPath, map).converter();

            DataSource source1 = new DataSource(trainingArffFile);
            Instances training = source1.getDataSet();
            DataSource source2 = new DataSource(testArffFile);
            Instances testing = source2.getDataSet();

            int numAttr = training.numAttributes();
            training.setClassIndex(numAttr - 1);
            testing.setClassIndex(numAttr - 1);

            RandomForest classifier1 = new RandomForest();
            evalModel(training, testing, classifier1, randomForest, "Random Forest", i);

            NaiveBayes classifier2 = new NaiveBayes();
            evalModel(training, testing, classifier2, naiveBayes, "Naive Bayes", i);


            IBk classifier3 = new IBk();
            evalModel(training, testing, classifier3, ibk, "IBk", i);
        }
    }

    private void evalModel(Instances training, Instances testing, Classifier classifier, double[] arr, String class_name, int i) throws Exception {
        Evaluation eval = new Evaluation(testing);
        classifier.buildClassifier(training);
        eval.evaluateModel(classifier, testing);
        List<String> arr1 = new ArrayList<>();
        arr[0] += eval.precision(1);
        arr[1] += eval.recall(1);
        arr[2] += eval.areaUnderROC(1);
        arr[3] += eval.kappa();
        arr1.add(projectName);
        arr1.add(Integer.toString(i+1));
        arr1.add(class_name);
        arr1.add(Double.toString(Math.round(eval.precision(1)*10000)/10000d));
        arr1.add(Double.toString(Math.round(eval.recall(1)*10000)/10000d));
        arr1.add(Double.toString(Math.round(eval.areaUnderROC(1)*10000)/10000d));
        arr1.add(Double.toString(Math.round(eval.kappa()*10000)/10000d));

        results.add(new ArrayList<>(arr1));
    }

    public void writeOutput2() throws IOException {
        File file = new File("./results_"+projectName.toLowerCase(Locale.ROOT)+".csv");
        FileWriter fileWriter = new FileWriter(file);
        CSVWriter writer = new CSVWriter(fileWriter);

        String[] header = {"Dataset", "#trainingReleases", "Classifier", "precision", "recall", "AUC", "kappa"};
        writer.writeNext(header);
        for(List<String> arr: results){
            String[] a = {arr.get(0), arr.get(1), arr.get(2), arr.get(3), arr.get(4), arr.get(5), arr.get(6)};
            writer.writeNext(a);
        }
        writer.close();
    }

}
