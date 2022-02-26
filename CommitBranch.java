package gitlet;

import java.io.Serializable;

/** The commit branch class.
 * @author David Long */
public class CommitBranch implements Serializable {

    /** The branch name of the commit branch. */
    private String _branchName;

    /** The commit hash of the first commit of this branch. */
    private String _headPointerHash;

    /** Constructor of the CommitBranch class
     * with inputs NAME and HASH.
     * @param name
     * @param hash
     */
    public CommitBranch(String name, String hash) {
        _branchName = name;
        _headPointerHash = hash;
    }

    /** Set the commit hash of the first commit of this branch to be HASH.
     * @param hash
     */
    public void setHeadPointerHash(String hash) {
        _headPointerHash = hash;
    }

    /** Getter method to get the name of the commit branch.
     * @return
     */
    public String getBranchName() {
        return _branchName;
    }

    /** The commit hash of the head commit of this branch.
     * @return
     */
    public String getHeadPointerHash() {
        return _headPointerHash;
    }

    @Override
    public String toString() {
        return _branchName;
    }
}
