package wekaAnalysis;

import com.opencsv.CSVWriter;
import csvfile.CSVList;
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

    public String projectName;
    public int releaseNumber=1;
    public List<CSVList> map;
    private double[] randomForest ={0,0,0,0};
    private double[] naiveBayes ={0,0,0,0};
    private double[] ibk = {0,0,0,0};
    private List<List<String>> results;

    public WekaParameters(String projectName, List<CSVList> map){
        this.projectName=projectName;
        this.releaseNumber=map.size()/2;
        this.map=map;
    }

    public void wekaAnalysis() throws Exception {

        results = new ArrayList<>();

        for (int i = 0; i < releaseNumber - 1; i++) {

            List<String> arr1 = new ArrayList<>();
            List<String> arr2 = new ArrayList<>();
            List<String> arr3 = new ArrayList<>();

            String testPath = TestingSet.getInstance(projectName.toLowerCase(Locale.ROOT), map).testingSetCreator();

            String testArffFile = new CSV2Arff(testPath, map).converter();

            String trainingPath = "./training_" + projectName.toLowerCase(Locale.ROOT) + (i + 1) + ".csv";

            String trainingArffFile = new CSV2Arff(trainingPath, map).converter();

            DataSource source1 = new DataSource(trainingArffFile);
            Instances training = source1.getDataSet();
            DataSource source2 = new DataSource(testArffFile);
            Instances testing = source2.getDataSet();

            int numAttr = training.numAttributes();
            training.setClassIndex(numAttr - 1);
            testing.setClassIndex(numAttr - 1);

            RandomForest classifier1 = new RandomForest();
            Evaluation eval = new Evaluation(testing);
            classifier1.buildClassifier(training);
            eval.evaluateModel(classifier1, testing);
            randomForest[0] += eval.precision(1);
            randomForest[1] += eval.recall(1);
            randomForest[2] += eval.areaUnderROC(1);
            randomForest[3] += eval.kappa();
            arr1.add(projectName);
            arr1.add(Integer.toString(i));
            arr1.add("Random Forest");
            arr1.add(Double.toString(eval.precision(1)));
            arr1.add(Double.toString(eval.recall(1)));
            arr1.add(Double.toString(eval.areaUnderROC(1)));
            arr1.add(Double.toString(eval.kappa()));

            NaiveBayes classifier2 = new NaiveBayes();
            eval = new Evaluation(testing);
            classifier2.buildClassifier(training);
            eval.evaluateModel(classifier2, testing);
            naiveBayes[0] += eval.precision(1);
            naiveBayes[1] += eval.recall(1);
            naiveBayes[2] += eval.areaUnderROC(1);
            naiveBayes[3] += eval.kappa();
            arr2.add(projectName);
            arr2.add(Integer.toString(i));
            arr2.add("NaiveBayes");
            arr2.add(Double.toString(eval.precision(1)));
            arr2.add(Double.toString(eval.recall(1)));
            arr2.add(Double.toString(eval.areaUnderROC(1)));
            arr2.add(Double.toString(eval.kappa()));


            IBk classifier3 = new IBk();
            eval = new Evaluation(testing);
            classifier3.buildClassifier(training);
            eval.evaluateModel(classifier3, testing);
            ibk[0] += eval.precision(1);
            ibk[1] += eval.recall(1);
            ibk[2] += eval.areaUnderROC(1);
            ibk[3] += eval.kappa();
            arr3.add(projectName);
            arr3.add(Integer.toString(i));
            arr3.add("IBk");
            arr3.add(Double.toString(eval.precision(1)));
            arr3.add(Double.toString(eval.recall(1)));
            arr3.add(Double.toString(eval.areaUnderROC(1)));
            arr3.add(Double.toString(eval.kappa()));

            results.add(arr1);
            results.add(arr2);
            results.add(arr3);
        }
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
