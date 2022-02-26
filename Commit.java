package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.Date;
import java.util.ArrayList;

/** The commit class.
 * @author David Long */
public class Commit implements Serializable {

    /** Commit Message. */
    private  String _commitMessage;

    /** Commit timestamp. */
    private Date _timestamp;

    /** Files that this commit have reference to. */
    private ArrayList<FileReference> _referenceFiles;

    /** Previous commits' Hash. */
    private LinkedList<String> _prevHash;

    /** Parent hash of this commit. */
    private String _parent;

    /** Default constructor of commit class with inputs
     * MESSAGE.
     */
    public Commit(String message) {
        _commitMessage = message;
        _timestamp = new Date();
        _referenceFiles = new ArrayList<>();
        _prevHash = new LinkedList<>();
    }

    /** First commit when repo is first created.
     * @return
     */
    public static Commit firstCommit() {
        return new Commit((long) 0, "initial commit");
    }

    /** Constructor of commit class
     * with inputs MESSAGE and TIMESTAMP.
     * @param timestamp
     * @param message
     * @return
     */
    public Commit(long timestamp, String message) {
        _timestamp = new Date(timestamp);
        _commitMessage = message;
        _referenceFiles = new ArrayList<>();
        _prevHash = new LinkedList<>();
    }

    /** Get Reference commits.
     * @return
     */
    public ArrayList<FileReference> getReferenceFiles() {
        return _referenceFiles;
    }

    /** Returns the names of all the Reference files tracked by this commit. */
    public ArrayList<String> getAllRefNames() {
        ArrayList<String> names = new ArrayList<>();
        for (FileReference r: _referenceFiles) {
            names.add(r.getFileName());
        }
        return names;
    }

    /** Get commit message.
     * @return
     */
    public String getCommitMessage() {
        return _commitMessage;
    }

    /**get timestamp.
     * @return
     */
    public Date getTimestamp() {
        return _timestamp;
    }

    /** Set Reference files to BEFORE.
     * @param before
     */
    public void setReferenceFiles(ArrayList<FileReference> before) {
        _referenceFiles = before;
    }

    /** Change commit hash of a file in referencefiles to FILE,
     * FILENAME, and NEWHASH.
     * @param file
     * @param fileName
     * @param newHash
     */
    public void addReferenceFiles(File file, String fileName, String newHash) {
        FileReference blob = filteredReferenceFile(fileName);
        if (blob == null) {
            _referenceFiles.add(new FileReference(file, fileName, newHash));
        } else if (blob != null) {
            blob.setReferenceHash(newHash);
        }
    }

    /**  Remove file with FILENAME from referencefiles.
     * @param fileName
     */
    public void removeReferenceFile(String fileName) {
        FileReference blob = filteredReferenceFile(fileName);
        if (blob != null) {
            _referenceFiles.remove(_referenceFiles.indexOf(blob));
        }
    }

    /** Add previous HASH to current commit.
     * @param hash
     */
    public void addPrevHash(String hash) {
        _prevHash.add(hash);
    }

    /** Returns the linkedlist containing all of parents of current commit.
     * @return
     */
    public LinkedList<String> getAllParentCommit() {
        return _prevHash;
    }

    /** Get the commit hash of the previous commit at INDEX.
     * @param index
     * @return
     */
    public String getPrevHash(int index) {
        if (_prevHash == null || index < 0 || index > _prevHash.size()) {
            return null;
        }
        return _prevHash.get(index);
    }

    /** Get the hash of the parent commit.
     * @return
     */
    public String getRecentParent() {
        if (_prevHash == null) {
            return null;
        }
        return _prevHash.get(_prevHash.size() - 1);
    }

    /** Set the HASH to be PARENT.
     * @param hash
     */
    public void setParent(String hash) {
        _parent = hash;
    }

    /** Check if this commit needs to be merged.
     * @return
     */
    public boolean isMerge() {
        if (_prevHash.size() == 0 || _prevHash.size() == 1) {
            return false;
        }
        return true;
    }

    /** A helper function to help loop through
     * reference files to get file FILENAME.
     * @param fileName
     * @return
     */
    public FileReference filteredReferenceFile(String fileName) {
        for (FileReference r: _referenceFiles) {
            if (r.getFileName().equals(fileName)) {
                return r;
            }
        }
        return null;
    }
}
