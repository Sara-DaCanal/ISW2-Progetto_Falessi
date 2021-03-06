package csvfile;

import org.eclipse.jgit.diff.Edit;
import java.util.List;

public class LOC {
    private long addedLines;
    private long deletedLines;
    private long modifiedLines;
    private long size;

    public LOC(List<Edit> editList, long size){
        this.size=size;
        this.addedLines=0;
        this.deletedLines=0;
        this.modifiedLines=0;
        for(Edit edit:editList){
            if(edit.getType() == Edit.Type.INSERT) this.addedLines += edit.getEndB() - edit.getBeginB();
            if(edit.getType() == Edit.Type.DELETE) this.deletedLines += edit.getEndA() - edit.getBeginA();
            if(edit.getType() == Edit.Type.REPLACE){
                replaceCase(edit);
            }
        }this.size+=(this.addedLines-this.deletedLines);

    }
    private void replaceCase(Edit edit){
        int diffA = edit.getEndA()-edit.getBeginA();
        int diffB = edit.getEndB()-edit.getBeginB();
        if(diffA==diffB) modifiedLines += diffA;
        if(diffA<diffB){
            modifiedLines += diffA;
            addedLines += diffB-diffA;
        }
        if(diffA>diffB){
            modifiedLines += diffB;
            deletedLines += diffA-diffB;
        }
    }

    public long getSize() {
        return size;
    }

    public long getLOCTouched(){
        return addedLines+modifiedLines+deletedLines;
    }

    public long getLOCAdded(){
        return this.addedLines;
    }


}
