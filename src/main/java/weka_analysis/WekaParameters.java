package weka_analysis;

import com.opencsv.CSVWriter;
import csvfile.CSVList;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.classifiers.Classifier;
import weka.classifiers.CostMatrix;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.CostSensitiveClassifier;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.Utils;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.supervised.instance.Resample;
import weka.filters.supervised.instance.SpreadSubsample;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WekaParameters {

    private String projectName;
    private int releaseNumber=1;
    private List<CSVList> map;
    private double[] randomForest ={0.0,0.0,0.0,0.0};
    private double[] naiveBayes ={0.0,0.0,0.0,0.0};
    private double[] ibk = {0.0,0.0,0.0,0.0};
    private List<List<String>> results;
    private List<AnalysisCSV> finalFile;
    private String dir;
    private String ncs = "No cost sensitive";

    public WekaParameters(String projectName, List<CSVList> map){
        this.projectName=projectName;
        this.releaseNumber=map.size()/2;
        this.map=map;
        this.dir="./file_"+projectName.toLowerCase();
    }

    public void wekaAnalysis() throws Exception {

        results = new ArrayList<>();
        finalFile = new ArrayList<>();

        for (int i = 0; i < releaseNumber - 1; i++) {

            String testPath = TestingSet.getInstance(projectName.toLowerCase(), map).testingSetCreator();

            String testArffFile = new CSV2Arff(testPath).converter();

            String trainingPath = dir+"/training_" + projectName.toLowerCase() + "_"+(i + 1) + ".csv";

            String trainingArffFile = new CSV2Arff(trainingPath).converter();

            AnalysisCSV finalLine = new AnalysisCSV(projectName, (i+1), trainingPath, testPath);

            DataSource source1 = new DataSource(trainingArffFile);
            Instances training = source1.getDataSet();
            DataSource source2 = new DataSource(testArffFile);
            Instances testing = source2.getDataSet();

            int numAttr = training.numAttributes();
            training.setClassIndex(numAttr - 1);
            testing.setClassIndex(numAttr - 1);


            String classname = "Random Forest";
            RandomForest classifier1 = new RandomForest();
            finalLine.setClassifier(classname);
            evalModel(training, testing, classifier1, randomForest, classname, i, AnalysisCSV.copyOf(finalLine));
            balancing(classifier1, AnalysisCSV.copyOf(finalLine), training, testing);

            classname="Naive Bayes";
            NaiveBayes classifier2 = new NaiveBayes();
            finalLine.setClassifier(classname);
            evalModel(training, testing, classifier2, naiveBayes, classname, i, AnalysisCSV.copyOf(finalLine));
            balancing(classifier2, AnalysisCSV.copyOf(finalLine), training, testing);

            classname = "IBk";
            IBk classifier3 = new IBk();
            finalLine.setClassifier(classname);
            evalModel(training, testing, classifier3, ibk, classname, i, AnalysisCSV.copyOf(finalLine));
            balancing(classifier3, AnalysisCSV.copyOf(finalLine), training, testing);

        }
        meanValues("Random Forest", randomForest);
        meanValues("Naive Bayes", naiveBayes);
        meanValues("Ibk", ibk);
        writeOutput2();
        writeResultsFile();
    }

    private void evalModel(Instances training, Instances testing, Classifier classifier, double[] arr, String className, int i, AnalysisCSV line) throws Exception {
        line.setBalancing("No sampling");
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
        arr1.add(className);
        arr1.add(Double.toString(Math.round(eval.precision(1)*100)/100d));
        arr1.add(Double.toString(Math.round(eval.recall(1)*100)/100d));
        arr1.add(Double.toString(Math.round(eval.areaUnderROC(1)*100)/100d));
        arr1.add(Double.toString(Math.round(eval.kappa()*100)/100d));
        bestFirst(training, testing, classifier, AnalysisCSV.copyOf(line));
        line.setFeatureSelection("No selection");
        costSensitive(training, testing, classifier, AnalysisCSV.copyOf(line));
        line.setSensitivity(ncs);
        line.setRates(eval.truePositiveRate(1), eval.falsePositiveRate(1), eval.trueNegativeRate(1), eval.falseNegativeRate(1));
        line.setMetrics(eval.precision(1), eval.recall(1),eval.areaUnderROC(1), eval.kappa());
        finalFile.add(line);
        results.add(new ArrayList<>(arr1));
    }

    private void meanValues(String className, double[] arr){
        List<String> arr1 = new ArrayList<>();
        arr1.add(projectName);
        arr1.add("MEDIA");
        arr1.add(className);
        for(double d: arr) arr1.add(Double.toString(Math.round(d/releaseNumber*100)/100d));
        results.add(new ArrayList<>(arr1));
    }

    private void balancing(Classifier classifier, AnalysisCSV line, Instances training, Instances testing ) throws Exception {
        AnalysisCSV underSampleLine = AnalysisCSV.copyOf(line);
        underSampleLine.setBalancing("Undersampling");
        SpreadSubsample spread = new SpreadSubsample();
        spread.setOptions(Utils.splitOptions("-M 1.0"));
        Evaluation eval = commonBalancing(spread, classifier, training, testing, underSampleLine);


        underSampleLine.setRates(eval.truePositiveRate(1), eval.falsePositiveRate(1), eval.trueNegativeRate(1), eval.falseNegativeRate(1));
        underSampleLine.setMetrics(eval.precision(1), eval.recall(1), eval.areaUnderROC(1), eval.kappa());

        finalFile.add(underSampleLine);

        AnalysisCSV overSampleLine = AnalysisCSV.copyOf(line);
        overSampleLine.setBalancing("Oversampling");
        Resample resample = new Resample();
        resample.setOptions(Utils.splitOptions("-B 1.0 -Z 130.3"));
        eval = commonBalancing(resample, classifier, training, testing, overSampleLine);


        overSampleLine.setRates(eval.truePositiveRate(1), eval.falsePositiveRate(1), eval.trueNegativeRate(1), eval.falseNegativeRate(1));
        overSampleLine.setMetrics(eval.precision(1), eval.recall(1), eval.areaUnderROC(1), eval.kappa());
        finalFile.add(overSampleLine);

    }
    private Evaluation commonBalancing(Filter resample, Classifier classifier, Instances training, Instances testing, AnalysisCSV line) throws Exception {
        FilteredClassifier filteredC = new FilteredClassifier();
        filteredC.setFilter(resample);
        filteredC.setClassifier(classifier);
        bestFirst(training, testing, filteredC, AnalysisCSV.copyOf(line));
        line.setFeatureSelection("No selection");
        costSensitive(training, testing,filteredC, AnalysisCSV.copyOf(line));
        line.setSensitivity(ncs);
        Evaluation eval = new Evaluation(testing);
        classifier.buildClassifier(training);
        eval.evaluateModel(classifier, testing);
        return eval;
    }

    private void bestFirst(Instances training, Instances testing, Classifier classifier, AnalysisCSV line) throws Exception {
        BestFirst bestFirst = new BestFirst();
        AttributeSelection filter = new AttributeSelection();
        CfsSubsetEval eval = new CfsSubsetEval();
        filter.setEvaluator(eval);
        filter.setSearch(bestFirst);
        filter.setInputFormat(training);
        Instances filteredTraining = Filter.useFilter(training, filter);
        int numAttrFiltered = filteredTraining.numAttributes();
        filteredTraining.setClassIndex(numAttrFiltered - 1);
        Instances testingFiltered = Filter.useFilter(testing, filter);
        testingFiltered.setClassIndex(numAttrFiltered - 1);
        Evaluation evalClass = new Evaluation(testingFiltered);
        classifier.buildClassifier(filteredTraining);
        evalClass.evaluateModel(classifier, testingFiltered);
        line.setFeatureSelection("Best First");
        costSensitive(training,testing, classifier, AnalysisCSV.copyOf(line));
        line.setSensitivity(ncs);
        line.setRates(evalClass.truePositiveRate(1), evalClass.falsePositiveRate(1), evalClass.trueNegativeRate(1), evalClass.falseNegativeRate(1));
        line.setMetrics(evalClass.precision(1), evalClass.recall(1), evalClass.areaUnderROC(1), evalClass.kappa());
        finalFile.add(line);
    }
    private void costSensitive(Instances training, Instances testing, Classifier classifier, AnalysisCSV line) throws Exception {
        CostSensitiveClassifier c1 = commonCostSensitive(classifier, training);
        c1.setMinimizeExpectedCost(true);
        Evaluation evalThreshold = new Evaluation(testing,c1.getCostMatrix());
        evalThreshold.evaluateModel(c1, testing);
        AnalysisCSV lineThresh = AnalysisCSV.copyOf(line);
        lineThresh.setSensitivity("Sensitive threshold");
        lineThresh.setRates(evalThreshold.truePositiveRate(1), evalThreshold.falsePositiveRate(1), evalThreshold.trueNegativeRate(1), evalThreshold.falseNegativeRate(1));
        lineThresh.setMetrics(evalThreshold.precision(1), evalThreshold.recall(1), evalThreshold.areaUnderROC(1), evalThreshold.kappa());
        finalFile.add(lineThresh);

        CostSensitiveClassifier c2 = commonCostSensitive(classifier, training);
        c2.setMinimizeExpectedCost(false);
        Evaluation evalLearning = new Evaluation(testing, c2.getCostMatrix());
        evalLearning.evaluateModel(c2, testing);
        line.setSensitivity("Sensitive learning");
        line.setRates(evalLearning.truePositiveRate(1), evalLearning.falsePositiveRate(1), evalLearning.trueNegativeRate(1), evalLearning.falseNegativeRate(1));
        line.setMetrics(evalLearning.precision(1), evalLearning.recall(1), evalLearning.areaUnderROC(1), evalLearning.kappa());
        finalFile.add(line);

    }

    private CostSensitiveClassifier commonCostSensitive(Classifier classifier, Instances training) throws Exception {
        CostSensitiveClassifier c1 = new CostSensitiveClassifier();
        c1.setClassifier(classifier);
        c1.setCostMatrix( createCostMatrix(1, 10));
        c1.buildClassifier(training);
        return c1;
    }
    private CostMatrix createCostMatrix(double weightFalsePositive, double weightFalseNegative) {
        CostMatrix costMatrix = new CostMatrix(2);
        costMatrix.setCell(0, 0, 0.0);
        costMatrix.setCell(1, 0, weightFalsePositive);
        costMatrix.setCell(0, 1, weightFalseNegative);
        costMatrix.setCell(1, 1, 0.0);
        return costMatrix;
    }

    private void writeResultsFile() throws IOException {
        try(CSVWriter writer = new CSVWriter(new FileWriter(new File(dir+"/final_"+projectName.toLowerCase()+".csv")))){
            String[] header = {"Dataset", "#trainingReleases", "%training", "%defectiveTraining", "%defectiveTesting", "classifier",
            "balancing", "featureSelection", "sensitivity", "TP", "FP", "TN", "FN", "precision", "recall", "AUC", "kappa"};
            writer.writeNext(header);
            for(AnalysisCSV c: finalFile){
                writer.writeNext(c.toStringArray());
            }
            writer.flush();
        }
    }


    private void writeOutput2() throws IOException {
        File file = new File(dir+"/results_"+projectName.toLowerCase()+".csv");
        FileWriter fileWriter = new FileWriter(file);
        try(CSVWriter writer = new CSVWriter(fileWriter)){

            String[] header = {"Dataset", "#trainingReleases", "Classifier", "precision", "recall", "AUC", "kappa"};
            writer.writeNext(header);
            for(List<String> arr: results){
                String[] a = {arr.get(0), arr.get(1), arr.get(2), arr.get(3), arr.get(4), arr.get(5), arr.get(6)};
                writer.writeNext(a);
            }
            writer.flush();
        }
    }

}
