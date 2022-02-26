package gitlet;

import java.io.File;
import java.io.Serializable;

/** Files that are tracked by commits.
 * @author David Long */
public class FileReference implements Serializable {

    /** File in a given commit. */
    private File _file;

    /** The name of the file. */
    private String _fileName;

    /** The Reference commit hash of the file. */
    private String _referenceHash;

    /** The version number of this file. */
    private int _version;

    /** Constructor of FileReference class
     * with inputs FILE FILENAME REFERENCEHASH.
     * @param file
     * @param fileName
     * @param referenceHash
     */
    public FileReference(File file, String fileName, String referenceHash) {
        _file = file;
        _fileName = fileName;
        _referenceHash = referenceHash;
        _version = 0;
    }

    /** Set ReferenceHash to NEWHASH.
     * @param newHash
     */
    public void setReferenceHash(String newHash) {
        _referenceHash = newHash;
        _version += 1;
    }

    /** File getter.
     * @return
     */
    public File getFile() {
        return _file;
    }

    /** Return the name of this file.
     * @return
     */
    public String getFileName() {
        return _fileName;
    }

    /** ReferenceHash Getter.
     * @return
     */
    public String getReferenceHash() {
        return _referenceHash;
    }

    /** Get the version of the file.
     * @return
     */
    public int getVersion() {
        return _version;
    }

    /** Updates the version of this blob. */
    public void updateVersion() {
        _version += 1;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        FileReference cast = (FileReference) other;
        return getFileName().equals(cast.getFileName())
                && getReferenceHash().equals(cast.getReferenceHash());
    }

    @Override
    public String toString() {
        return _file + " - " + _referenceHash;
    }
}
