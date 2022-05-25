package csvfile;

public class Proportion {
    private static Proportion me=null;
    private double p;
    private int n;

    private Proportion(){
        p=0.0;
        n=0;
    }

    public void setP(int fv, int iv, int ov){
        n++;
        if(iv<fv && ov<fv){
            p= p + (double)(fv-iv)/(fv-ov);
        }
    }

    public double getP(){
        if(n==0) return 0;
        return p/n;
    }

    public static Proportion getProportion(){
        if(me == null) me= new Proportion();
       return me;
    }
}
