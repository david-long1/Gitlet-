package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** The staging area class.
 * @author David Long */
public class StagingArea implements Serializable {

    /** The directory containing files staged for addition. */
    private static final File ADDITION_FOLDER =
            Utils.join(Main.STAGING_FOLDER, "addition");

    /** The directory containing files staged for removal. */
    private static final File REMOVAL_FOLDER =
            Utils.join(Main.STAGING_FOLDER, "removal");

    /** List of file names that are staged for addition. */
    private ArrayList<String> _addition;

    /** List of file names that are staged for removal. */
    private ArrayList<String> _removal;

    /** Constructor of Staging Area class. */
    public StagingArea() {
        _addition = new ArrayList<>();
        _removal = new ArrayList<>();
        ADDITION_FOLDER.mkdir();
        REMOVAL_FOLDER.mkdir();
    }

    /** Get addition area.
     * @return
     */
    public ArrayList<String> getAdditionArea() {
        return _addition;
    }

    /** Get removal area.
     * @return
     */
    public ArrayList<String> getRemovalArea() {
        return _removal;
    }

    /** Get staged file FILENAME for addition.
     * @param fileName
     * @return
     */
    public File getStagedAdditionFile(String fileName) {
        return Utils.join(ADDITION_FOLDER, fileName);
    }

    /** Clear file FILENAME staged for addition.
     * @param fileName
     */
    public void cancelStagedAddition(String fileName) {
        String fN = filteredAddition(fileName);
        if (fN != null) {
            _addition.remove(fN);
            File addition = Utils.join(ADDITION_FOLDER, fileName);
            addition.delete();
        }
    }

    /** Stage file FILENAME for addition.
     * @param fileName
     */
    public void stageForAddition(String fileName) {
        String fN = filteredAddition(fileName);
        if (fN == null) {
            _addition.add(fileName);
            Utils.writeContents(Utils.join(ADDITION_FOLDER, fileName),
                    Utils.readContents(Utils.join(Main.CWD, fileName)));
        }
    }

    /** Delete all files staged for addition. */
    public void deleteAllAddition() {
        _addition.clear();
        List<String> additionFiles = Utils.plainFilenamesIn(ADDITION_FOLDER);
        for (String additionName: additionFiles) {
            File addition = Utils.join(ADDITION_FOLDER, additionName);
            addition.delete();
        }
    }

    /** Get file FILENAME that is staged for removal.
     * @param fileName
     * @return
     */
    public File getStagedRemovalFile(String fileName) {
        return Utils.join(REMOVAL_FOLDER, fileName);
    }

    /** Clear file FILENAME staged for removal.
     * @param fileName
     */
    public void cancelStagedRemovel(String fileName) {
        String fN = filteredRemoval(fileName);
        if (fN != null) {
            _removal.remove(fN);
            File removal = Utils.join(REMOVAL_FOLDER, fileName);
            removal.delete();
        }
    }

    /** Stage file FILENAME for removal.
     * @param fileName
     */
    public void stageForRemoval(String fileName) {
        String fN = filteredRemoval(fileName);
        if (fN == null) {
            _removal.add(fileName);
            Utils.writeContents(Utils.join(REMOVAL_FOLDER, fileName),
                    Utils.readContents(Utils.join(Main.CWD, fileName)));
        }
    }

    /** Manually stage for remove FILENAME.
     * @param fileName
     */
    public void manualAddRemoval(String fileName) {
        String fN = filteredRemoval(fileName);
        if (fN == null) {
            _removal.add(fileName);
        }
    }

    /** Delete all files staged for removal.*/
    public void deleteAllRemoval() {
        _removal.clear();
        List<String> removalFiles = Utils.plainFilenamesIn(REMOVAL_FOLDER);
        for (String additionName: removalFiles) {
            File removal = Utils.join(REMOVAL_FOLDER, additionName);
            removal.delete();
        }
    }

    /** Delete all files in the staging area. */
    public void deleteAllStage() {
        deleteAllAddition();
        deleteAllRemoval();
    }

    /** A helper function to help loop through files
     * that are staged for removal to get file FILENAME.
     * @param fileName
     * @return
     */
    public String filteredRemoval(String fileName) {
        for (String f: _removal) {
            if (f.equals(fileName)) {
                return f;
            }
        }
        return null;
    }

    /** A helper function to help loop through files
     * that are staged for addition to get file FILENAME.
     * @param fileName
     * @return
     */
    public String filteredAddition(String fileName) {
        for (String f: _addition) {
            if (f.equals(fileName)) {
                return f;
            }
        }
        return null;
    }
}
