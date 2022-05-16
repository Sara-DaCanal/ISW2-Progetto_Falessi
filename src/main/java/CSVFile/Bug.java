package CSVFile;

public class Bug {
    private String key;
    private Version affectedVersion;
    private Version fixedVersion;

    public Bug(String k){
        this.key=k;
        this.affectedVersion=null;
        this.fixedVersion=null;
    }
    public Bug(String k, Version av, Version fv){
        this.key=k;
        this.fixedVersion=fv;
        this.affectedVersion=av;
    }

    public Version getAffectedVersion() {
        return affectedVersion;
    }

    public String getKey() {
        return key;
    }

    public Version getFixedVersion() {
        return fixedVersion;
    }

    public void setAffectedVersion(Version affectedVersion) {
        this.affectedVersion = affectedVersion;
    }

    public void setFixedVersion(Version fixedVersion) {
        this.fixedVersion = fixedVersion;
    }

    public void setKey(String key) {
        this.key = key;
    }

}
