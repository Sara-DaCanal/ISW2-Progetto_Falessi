package csvfile;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Version{
    private Date releaseDate;
    private String name="";

    public Version(){}
    public Version(String s, Date d){
        this.name=s;
        this.releaseDate=d;
    }

    public void setReleaseDate(String date) throws ParseException {
        this.releaseDate = new SimpleDateFormat("yyyy-MM-dd").parse(date);
    }
    public Date getReleaseDate(){
        return this.releaseDate;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getName(){
        return this.name;
    }
    @Override
    public boolean equals(Object v){
        return this.name.equals(((Version) v).name);
    }

}
