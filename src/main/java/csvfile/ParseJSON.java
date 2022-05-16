package csvfile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ParseJSON {

    private List<Version> verList = new ArrayList<>();
    private String projectName;
    private List<Bug> bugList = new ArrayList<>();


    public void setProjectName(String name){
        this.projectName=name;
    }
    public String getProjectName(){ return this.projectName; }

    public List<Version> getVersionArray() throws JSONException, ParseException, IOException {

        int total;
        int j;
        int i=0;
        do {
            //Only gets a max of 1000 at a time, so must do this multiple times if bugs >1000
            j = i + 1000;
            String url = "https://issues.apache.org/jira/rest/api/2/project/"
                    + this.projectName + "/version?" + "&maxResults=" + j + "&startAt=" + i;
            JSONObject json = readJsonFromUrl(url);
            JSONArray values = json.getJSONArray("values");
            total = json.getInt("total");
            for (; i < total && i < j; i++) {
                //Iterate through each bug
                JSONObject ver = values.getJSONObject(i % 1000);
                if (!ver.isEmpty() && ver.get("released").toString().contentEquals( "true")) {
                    Version y = new Version();
                    y.setName(ver.get("name").toString());
                    y.setReleaseDate(ver.get("releaseDate").toString());
                    if(!is(y)) this.verList.add(y);
                }
            }
        }while (i < total);
        Integer length=this.verList.size();
        mergeSort(this.verList, length);
        return this.verList;
    }

    public List<Bug> getBugList() throws IOException, ParseException {
        int i=0;
        int j;
        int total;
        String relDate = "releaseDate";
        do {
            j=1000+i;
            String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22"
                    + this.projectName + "%22AND%22issueType%22=%22Bug%22AND%20affectedVersion%20is%20not%20EMPTY%20AND%20fixVersion%20is%20not%20EMPTY%20AND(%22status%22=%22closed%22OR"
                    + "%22status%22=%22resolved%22)AND%22resolution%22=%22fixed%22&fields=key,fixVersions,versions&created&startAt="
                    + 0 + "&maxResults=" + 1000;
            JSONObject json = readJsonFromUrl(url);
            JSONArray issues = json.getJSONArray("issues");
            total = json.getInt("total");
            for (; i < total && i < j; i++) {
                String key = issues.getJSONObject(i % 1000).get("key").toString();
                Bug b = new Bug(key);
                JSONArray fixVer = issues.getJSONObject(i%1000).getJSONObject("fields").getJSONArray("fixVersions");
                Version fv = new Version();
                fv.setName(fixVer.getJSONObject(0).get("name").toString());
                fv.setReleaseDate(fixVer.getJSONObject(0).get(relDate).toString());
                for(int k=1;k<fixVer.length();k++){
                    if(fv.getReleaseDate().before(new SimpleDateFormat("yyyy-MM-dd").parse(fixVer.getJSONObject(k).get(relDate).toString()))){
                        fv.setReleaseDate(fixVer.getJSONObject(k).get(relDate).toString());
                        fv.setName(fixVer.getJSONObject(k).get("name").toString());
                    }
                }
                JSONArray affVer = issues.getJSONObject(i%1000).getJSONObject("fields").getJSONArray("versions");
                Version av = new Version();
                av.setName(affVer.getJSONObject(0).get("name").toString());
                av.setReleaseDate(affVer.getJSONObject(0).get(relDate).toString());
                for(int k=1;k<affVer.length();k++){
                    if(av.getReleaseDate().before(new SimpleDateFormat("yyyy-MM-dd").parse(affVer.getJSONObject(k).get(relDate).toString()))){
                        av.setReleaseDate(affVer.getJSONObject(k).get(relDate).toString());
                        av.setName(affVer.getJSONObject(k).get("name").toString());
                    }
                }
                b.setAffectedVersion(av);
                b.setFixedVersion(fv);
                this.bugList.add(b);
            }
        }while(i<total);

        return this.bugList;
    }

    private static void merge(List<Version> leftArr, List<Version> rightArr, List<Version> arr, Integer leftSize, Integer rightSize){

        int i=0;
        int l=0;
        int r = 0;
        //The while loops check the conditions for merging
        while(l<leftSize && r<rightSize){

            if(leftArr.get(l).getReleaseDate().before(rightArr.get(r).getReleaseDate())){
                arr.set(i,leftArr.get(l));
                i++;
                l++;
            }
            else{
                arr.set(i, rightArr.get(r));
                i++;
                r++;
            }
        }
        while(l<leftSize){
            arr.set(i++, leftArr.get(l++));
        }
        while(r<rightSize){
            arr.set(i++, rightArr.get(r++));
        }
    }

    private static void mergeSort(List<Version> list, Integer len){
        if (len < 2){return;}

        int mid = len / 2;
        List<Version> leftArr = new ArrayList<>(list.subList(0,mid));
        List<Version> rightArr = new ArrayList<>(list.subList(mid, len));

        //Dividing array into two and copying into two separate arrays
        int k = 0;
        for(int i = 0;i<len;++i){
            if(i<mid){
                leftArr.set(i,list.get(i));
            }
            else{
                rightArr.set(k, list.get(i));
                k = k+1;
            }
        }
        // Recursively calling the function to divide the subarrays further
        mergeSort(leftArr,mid);
        mergeSort(rightArr,len-mid);
        // Calling the merge method on each subdivision
        merge(leftArr,rightArr,list,mid,len-mid);
    }

    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();
        try (
                BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));){
            String jsonText = readAll(rd);
            return new JSONObject(jsonText);}
         catch(IOException | JSONException e) {
            is.close();
            return null;
        }
    }
    private boolean is(Version v){
        for (Version i: this.verList) {
            if (v.getReleaseDate().compareTo(i.getReleaseDate()) == 0) {
                return true;
            }
        }
        return false;
    }
    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }
}
