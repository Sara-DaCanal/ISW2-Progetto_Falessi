import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.lang.module.ModuleDescriptor;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class ParseJSON {

    private List<Version> verList = new ArrayList<>();
    private String projectName;


    public void setProjectName(String name){
        this.projectName=name;
    }

    public List<Version> getVersionArray() throws JSONException, ParseException, IOException {

        int total, j, i=0;
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
                if (!ver.isEmpty()) {
                    if(ver.get("released").toString().contentEquals( "true")){
                        Version y = new Version();
                        y.setName(ver.get("name").toString());
                        y.setReleaseDate(ver.get("releaseDate").toString());
                        if(!is(y)) this.verList.add(y);
                    }
                }
            }
        }while (i < total);
        Integer length=this.verList.size();
        mergeSort(this.verList, length);
        return this.verList;
    }

    private static void merge(List<Version> left_arr,List<Version> right_arr, List<Version> arr,Integer left_size, Integer right_size){

        int i=0,l=0,r = 0;
        //The while loops check the conditions for merging
        while(l<left_size && r<right_size){

            if(left_arr.get(l).getReleaseDate().before(right_arr.get(r).getReleaseDate())){
                arr.set(i,left_arr.get(l));
                i++;
                l++;
            }
            else{
                arr.set(i, right_arr.get(r));
                i++;
                r++;
            }
        }
        while(l<left_size){
            arr.set(i++, left_arr.get(l++));
        }
        while(r<right_size){
            arr.set(i++, right_arr.get(r++));
        }
    }

    private static void mergeSort(List<Version> list, Integer len){
        if (len < 2){return;}

        int mid = len / 2;
        List<Version> left_arr = new ArrayList<>(list.subList(0,mid));
        List<Version> right_arr = new ArrayList<>(list.subList(mid, len));

        //Dividing array into two and copying into two separate arrays
        int k = 0;
        for(int i = 0;i<len;++i){
            if(i<mid){
                left_arr.set(i,list.get(i));
            }
            else{
                right_arr.set(k, list.get(i));
                k = k+1;
            }
        }
        // Recursively calling the function to divide the subarrays further
        mergeSort(left_arr,mid);
        mergeSort(right_arr,len-mid);
        // Calling the merge method on each subdivision
        merge(left_arr,right_arr,list,mid,len-mid);
    }

    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JSONObject json = new JSONObject(jsonText);
            return json;
        } finally {
            is.close();
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
