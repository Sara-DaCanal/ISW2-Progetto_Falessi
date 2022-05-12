import org.eclipse.jgit.diff.Edit;
import java.util.List;

public class LOC {
    private List<Edit> editList;
    private long addedLines;
    private long deletedLines;
    private long modifiedLines;

    public LOC(List<Edit> editList){
        this.editList=editList;
        this.addedLines=0;
        this.deletedLines=0;
        this.modifiedLines=0;
        for(Edit edit:editList){
            if(edit.getType() == Edit.Type.INSERT) this.addedLines += edit.getEndB() - edit.getBeginB();
            if(edit.getType() == Edit.Type.DELETE) this.deletedLines += edit.getEndA() - edit.getBeginA();
            if(edit.getType() == Edit.Type.REPLACE){
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
        }
    }

    public long getSize() {
        return addedLines - deletedLines;
    }

    public long getLOCTouched(){
        return addedLines+modifiedLines+deletedLines;
    }

    public long getLOCAdded(){
        return this.addedLines;
    }


}
