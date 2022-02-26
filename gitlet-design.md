# Gitlet Design Document

**Name**: David Long

## Classes and Data Structures
### CommitBranch

This class represents a commit branch (i.e. master branch).

**Fields**
1. private String _branchName: The branch name of the commit branch.
2. private String _headPointerHash: The commit hash of the commit pointed by the head pointer of this branch.

### FileReference

This class represents blob files that are tracked by commits.

**Fields**
1. final static int RND = (int) System.currentTimeMillis(): Used in computing hashcode.
2. private File _file: The blob file referenced by commits.
3. private String _referenceHash: The hash of this blob file.
4. private int _version: The version number of this blob file. Changing the file will update its version.

### Commit

This class represents commits created by command commit.

**Fields**
1. private  String _commitMessage: The commit message.
2. private Date _timestamp: The commit timestamp.
3. private ArrayList<FileReference> _referenceFiles: Files that referenced/tracked by this commit.
4. private LinkedList<String> _prevHash: A linked list that contains all of the hash of commits made before this commit.
5. private String _parent: Parent hash of this commit.

### Work

This class process the majority of work of every command so as to make Main.java more concise.

**Fields**
1. private String _currheadHash: The hash of the commit which is on the current branch and  pointed by head pointer.
2. private String _currBranch: The branch currently on.
3. private ArrayList<CommitBranch> _branches: List of all of the commit branches.
4. private StagingArea _stagingArea: Staging Area.
5. private HashMap<String, Commit> _currBranchCommitHistory: Maps the hash of a commit to the commit itself, which only
applies to the commits on the current branch.

###Main

This class is where the input commands are processed and executed.

**Fields**
1. private static final File CWD = new File(System.getProperty("user.dir")): Current working directory.
2. public static final File GITLET_FOLDER = Utils.join(CWD, ".gitlet"): Gitlet directory.
3. private static final File WORK_FILE = Utils.join(GITLET_FOLDER, "work"): Work file.
4. public static final File STAGING_FOLDER = Utils.join(GITLET_FOLDER, ".stagings"): Staging directory that contains
files either staged for addition or removal.
5. private static final File COMMIT_FOLDER = Utils.join(GITLET_FOLDER, "commits"): Commit directory that contains
all the commits.
6. private static final File BLOB_FOLDER = Utils.join(GITLET_FOLDER, "blobs"): Blob directory that contains all the blobs.
7. private static final String MASTER_BRANCH = "master": The master branch.

### StagingArea

This class represents the staging area.

**Fields**
1. private HashMap<String, File> _addition: Maps the hash of files that are staged for addition to the files themselves.
2. private HashMap<String, File> _removal: Maps the hash of files that are staged for removal to the files themselves.
## Algorithms

### CommitBranch
1. CommitBranch(String name, String Hash): Constructor of the CommitBranch class with inputs NAME and HASH.
2. setHeadPointerHash(String Hash): Set the head pointer to point to the commit with commit hash HASH.
3. getBranchName(): Get the name of the commit branch.
4. toString: Overrrides the original toString method to print out the branch name and the hash of the commit
pointed by the head pointer.

### FileReference
1. FileReference(File file, String referenceHash): Constructor of FileReference class with inputs FILE and REFERENCE HASH.
2. FileReference(String fileName, String referenceHash): Second Constructor in case the file name instead of the file
is passed in.
3. setReferenceHash(String newHash): Set the reference file/blob to have a new hash after the content in it changed.
4. File getFile(): Get the blob.
5. getReferenceHash(): Get the hash of the blob.
6. getVersion(): Get the version of this blob.
7. hashCode(): Overrides the hash code.
8. equals(Object other): Overrides equals method.
9. toString(): Overrides toString() method.

###Commit
1. Commit(String message): efault constructor of commit class with inputs
MESSAGE.
2. Commit firstCommit(): Static method to get first commit when repo is first created.
3. Commit(long timestamp, String message): Constructor of commit class with inputs MESSAGE and TIMESATMP.
4. ArrayList<FileReference> getReferenceFiles(): Get the blobs tracked by this commit.
5. String getCommitMessage(): Get the commit message.
6. getTimestamp(): Get the time stamp of this commit.
7. setReferenceFiles(ArrayList<FileReference> before): Set the blobs tracked by this commit to be BEFORE, 
which is oftened used when blobs tracked by previous commits do not change.
8. addReferenceFiles(String fileName, String newHash): Track a new blob if not already tracked.
If already tracked, overrides its hash code with newHash.
9. removeReferenceFile(String fileName): Untrack a blob.
10. addPrevHash(String hash): Add a parent of this commit.
11. getPrevHash(int index): et the commit hash of the previous commit at INDEX.
12. String getParent(): Get the hash of the parent commit.
13. setParent(): Set the parent of this commit to be PARENT.
14. FileReference getCopyReference(String fileName): Get the copy of the reference file corresponding FILENAME.
15. FileReference filteredReferenceFile(String fileName):  helper function to help loop through reference files
to get file FILENAME.

###Work
1. Work(): Constructor of Work class.
2. getCurrBranchCommitHistory(): Get the commit history of current branch.
3. updateCurrBranchCommitHistory(String hash, Commit commit): Updates the commit history of the current branch.
4. StagingArea getStagingArea(): Get the staging area.
5. addBranchHash(String branchName, String hash): Add branch BRANCHNAME with HASH if there is no such branch. 
Otherwise, set the hash of BRANCHNAME to HASH.
6. removeBranch(String branchName): Remove branch BRANCHNAME if there exists such branch.
7. setCurrHeadHash(String hash): Set the head pointer of current branch to be HASH.
8. String getCurrHeadHash(): Get the current commit pointed by the head pointer.
9. setCurrBranch(String branch): set the current branch to be BRANCH.
10. String getCurrBranch(): Get the current branch.
11. filteredBranch(String branchName): A helper function to help loop through commit branches to get branch BRANCHNAME.

###StagingArea
1. StagingArea(): Constructor of Staging Area class, which initializes private fields.
2. HashMap<String, File> getAdditionArea(): Get addition area.
3. HashMap<String , File> getRemovalArea(): Get removal area.
4. getStagedAdditionFile(String fileName): Get staged file FILENAME.
5. cancelStagedAddition(String fileName): Clear file FILENAME staged for addition.
6. stageForAddition(String fileName): Stage file FILENAME for addition.
7. deleteAllAddition(): Delete all files staged for addition.
8. getStagedRemovalFile(String fileName): Get file FILENAME that is staged for removal.
9. cancelStagedRemovel(String fileName): Clear file FILENAME staged for removal.
10. stageForRemoval(String fileName): Stage file FILENAME for removal.
11. deleteAllRemoval(): Delete all files staged for removal.
12. filteredRemoval(String fileName): A helper function to help loop through files
that are staged for removal to get file FILENAME.
13. filteredAddition(String fileName): A helper function to help loop through files
that are staged for addition to get file FILENAME.

###Main
1. init(): Implements init functionality of Gitlet.
2. add(String fileName) throws IOException: Implements add functionality of Gitlet. Stage file FILENAME. 
Get file using join and read object from file, and do some operation and write back.
3. commit(String message): Implements commit functionality of Gitlet. Commit changes with commit MESSAGE.
4. rm(String fileName): Implements remove functionality of Gitlet by removing file FILENAME from Reference files that are tracked by commit.
5. serializeWork(Work work): Helper method that help serializes WORK as Work file.
6. deserializeWork(): Helper method that help deserializes the Work file and returns the deserialized file for future use.
7. serializeCommit(Commit commitObj): Static method that serializes commit COMM and stores it in the commits folder. 
Returns SHA1 hash (filename).
8. getCommitHash(Commit commitObj): Help to get the hashcode of a commit.
9. getFileHash(File f): Help to get the a hash code of a file.
10. deserializeCommit(String hash): Deserializes commit file HASH and returns Commit object.
11. getCurrHeadCommit(Work work): Get the current head commit.
12. getstagingFileNames(): Get the Names of the files in staging folder.
13. serializeBlob(File blob): Static method that serializes file BLOB and
stores it in the blob foolder.



## Persistence
 * File persistence is maintained in Main.java.
 * Work is serialized by using readObject when needed, and it is deserialzied by using writeObject when needed.
 * Commit is serialized by using readObject when needed, and it is deserialzied by using writeObject when needed.
