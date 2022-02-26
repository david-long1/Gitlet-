package gitlet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Locale;
import java.util.Collections;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author David Long
 */
public class Main {

    /** Current Working Directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));

    /** Gitlet folder. */
    public static final File GITLET_FOLDER = Utils.join(CWD, ".gitlet");

    /** Work folder. */
    public static final File WORK_FILE = Utils.join(GITLET_FOLDER, "work");

    /** Staging area folder. */
    public static final File STAGING_FOLDER =
            Utils.join(GITLET_FOLDER, "staging");

    /** Remote directories folder. */
    public static final File REMOTE_FOLDER =
            Utils.join(GITLET_FOLDER, "remote");

    /** Commit folder. */
    public static final File COMMIT_FOLDER =
            Utils.join(GITLET_FOLDER, "commits");

    /** Blob folder. */
    public static final File BLOB_FOLDER = Utils.join(GITLET_FOLDER, "blobs");

    /** Master Bracn. */
    public static final String MASTER_BRANCH = "master";

    /** Used in merge. */
    private static ArrayList<String> _usedNames = new ArrayList<>();

    /** Getter of used names.
     * @return
     */
    public ArrayList<String> getUsedNames() {
        return _usedNames;
    }

    /** Check EXPLENGTH ARGS.
     * @param expLength
     * @param args
     * @return
     */
    private static void checkOperands(int expLength, String[] args) {
        boolean error = false;
        if (expLength != args.length) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }

    }

    /** Check if .gitlet is contained.
     * @return
     */
    private static void containGitlet() {
        boolean error = false;
        if (!Files.exists(Paths.get(".gitlet"))) {
            System.out.println(("Not in an initialized Gitlet directory."));
            System.exit(0);
        }
    }

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            return;
        }
        switch (args[0]) {
        case"init":
            checkOperands(1, args);
            init();
            break;
        case "add":
            containGitlet();
            checkOperands(2, args);
            add(args[1]);
            break;
        case "commit":
            containGitlet();
            checkOperands(2, args);
            commit(args[1]);
            break;
        case "rm":
            containGitlet();
            checkOperands(2, args);
            rm(args[1]);
            break;
        case "log":
            containGitlet();
            log();
            break;
        case "global-log":
            containGitlet();
            globalLog();
            break;
        case "find":
            checkOperands(2, args);
            find(args[1]);
            break;
        case "checkout":
            containGitlet();
            if (args.length == 3 && args[1].equals("--")) {
                checkOperands(3, args);
                checkout(args[2]);
                break;
            } else if (args.length == 4 && args[2].equals("--")) {
                checkOperands(4, args);
                checkout(args[1], args[3]);
                break;
            } else if (args.length == 2) {
                checkoutBranch(args[1]);
                break;
            } else {
                System.out.println("Incorrect operands.");
                return;
            }
        default:
            mainRest(args);
        }
        return;
    }

    /** ARGS. */
    public static void mainRest(String... args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            return;
        }
        switch (args[0]) {
        case "status":
            containGitlet();
            status();
            break;
        case "branch":
            containGitlet();
            checkOperands(2, args);
            branch(args[1]);
            break;
        case "rm-branch":
            containGitlet();
            checkOperands(2, args);
            rmBranch(args[1]);
            break;
        case "reset":
            containGitlet();
            checkOperands(2, args);
            reset(args[1]);
            break;
        case "merge":
            containGitlet();
            checkOperands(2, args);
            merge(args[1]);
            break;
        case "add-remote":
            containGitlet();
            checkOperands(3, args);
            addRemote(args[1], args[2]);
            break;
        case "rm-remote":
            containGitlet();
            checkOperands(2, args);
            rmRemote(args[1]);
            break;
        case "push":
            containGitlet();
            checkOperands(3, args);
            push(args[1], args[2]);
            break;
        case "fetch":
            containGitlet();
            checkOperands(3, args);
            fetch(args[1], args[2]);
            break;
        case "pull":
            containGitlet();
            checkOperands(3, args);
            push(args[1], args[2]);
            break;
        default:
            System.out.println("No command with that name exists.");
        }
        return;
    }
    /** Implements init functionality. */
    private static void init() {
        if (Files.exists(Paths.get(".gitlet/"))) {
            System.out.println("A Gitlet verson-control system already"
                    + "exists in the current directory.");
            return;
        }
        GITLET_FOLDER.mkdir();
        STAGING_FOLDER.mkdir();
        REMOTE_FOLDER.mkdir();
        COMMIT_FOLDER.mkdir();
        BLOB_FOLDER.mkdir();
        try {
            WORK_FILE.createNewFile();
        } catch (IOException ie) {
            System.out.println("error");
        }
        Commit first = Commit.firstCommit();
        String hash = getCommitHash(first);
        Work work = new Work();
        work.addBranchHash(MASTER_BRANCH, hash);
        work.setCurrHeadHash(hash);
        work.setCurrBranch(MASTER_BRANCH);
        work.updateCurrBranchCommitHistory(first);
        serializeCommit(first);
        serializeWork(work);
    }

    /** Implements add functionality. Stage file FILENAME.
     * Get file using join and read object from file,
     * and do some operation and write back.
     * @param fileName
     */
    public static void add(String fileName) {
        if (!Utils.join(CWD, fileName).exists()) {
            System.out.println("File does not exist.");
            return;
        }
        Work work = deserializeWork();
        Commit head = getCurrHeadCommit(work);
        ArrayList<FileReference> prev = head.getReferenceFiles();
        StagingArea stage = work.getStagingArea();
        String fileHash = getFileHash(Utils.join(CWD, fileName));
        boolean inCommit = false;
        for (FileReference r: prev) {
            if (fileName.equals(r.getFileName())
                    && fileHash.equals(r.getReferenceHash())) {
                inCommit = true;
                break;
            }
        }
        if (inCommit) {
            stage.cancelStagedAddition(fileName);
        } else {
            stage.stageForAddition(fileName);
        }
        stage.cancelStagedRemovel(fileName);
        serializeWork(work);
    }

    /** Implements commit functionality.
     * Commit changes with commit MESSAGE.
     * @param message
     */
    public static void commit(String message) {
        if (message.isBlank() || message.isEmpty()
                || message == null) {
            System.out.println("Please enter a commit message.");
            return;
        }
        Work work = deserializeWork();
        Commit head = getCurrHeadCommit(work);
        ArrayList<FileReference> prev = head.getReferenceFiles();
        Commit toBeCommited = new Commit(message);
        toBeCommited.setReferenceFiles(prev);
        toBeCommited.addPrevHash(work.getCurrHeadHash());
        toBeCommited.setParent(work.getCurrHeadHash());
        StagingArea stage = work.getStagingArea();
        ArrayList<String> additionArea = stage.getAdditionArea();
        ArrayList<String> removalArea = stage.getRemovalArea();
        if (additionArea.size() == 0
                && removalArea.size() == 0) {
            System.out.println("No changes added to the commit.");
            return;
        }
        for (String fileName: additionArea) {
            File stagedFile = stage.getStagedAdditionFile(fileName);
            String fileHash = getFileHash(stagedFile);
            FileReference oldVersion =
                    toBeCommited.filteredReferenceFile(fileName);
            if (oldVersion != null) {
                oldVersion.updateVersion();
            }
            toBeCommited.addReferenceFiles(stagedFile, fileName, fileHash);
            serializeBlob(stagedFile);
        }
        stage.deleteAllAddition();
        for (String fileName: removalArea) {
            toBeCommited.removeReferenceFile(fileName);
        }
        stage.deleteAllRemoval();
        work.updateCurrBranchCommitHistory(toBeCommited);
        serializeCommit(toBeCommited);
        work.setCurrHeadHash(getCommitHash(toBeCommited));
        work.addBranchHash(work.getCurrBranch(), getCommitHash(toBeCommited));
        serializeWork(work);
    }

    /** Helper method that help serializes WORK as Work file.
     * @param work
     */
    private static void serializeWork(Work work) {
        Utils.writeObject(WORK_FILE, work);
    }

    /** Helper method that help deserializes the Work file
     * and returns the deserialized file for future use.
     * @return
     */
    private static Work deserializeWork() {
        return Utils.readObject(WORK_FILE, Work.class);
    }

    /** Serializes file BLOB and
     * stores it in the blob foolder.
     * @param blob
     */
    private static void serializeBlob(File blob) {
        Utils.writeContents(Utils.join(BLOB_FOLDER, getFileHash(blob)),
                Utils.readContents(blob));
    }

    /** Deserizes blob file HASH and return the file.
     * @param hash
     * @return
     */
    private static File deserializeBlob(String hash) {
        return Utils.join(BLOB_FOLDER, hash);
    }

    /** Static method that serializes commit COMMITOBJ and
     * stores it in the commits folder. Returns SHA1 hash (filename).
     * @param commitObj
     */
    private static void serializeCommit(Commit commitObj) {
        Utils.writeObject(Utils.join(
                COMMIT_FOLDER, getCommitHash(commitObj)), commitObj);
    }

    /** Help to get the hashcode of a COMMITOBJ.
     * @param commitObj
     * @return
     */
    private static String getCommitHash(Commit commitObj) {
        String hash = Utils.sha1(Utils.serialize(commitObj));
        return hash;
    }

    /** Help to get the a hash code of a F.
     * @param f
     * @return
     */
    private static String getFileHash(File f) {
        return Utils.sha1(Utils.readContents(f));
    }

    /** Deserializes commit file HASH and returns Commit object.
     * @param hash
     * @return
     */
    public static Commit deserializeCommit(String hash) {
        return Utils.readObject(Utils.join(COMMIT_FOLDER, hash), Commit.class);
    }

    /** Get the current head commit WORK.
     * @param work
     * @return
     */
    public static Commit getCurrHeadCommit(Work work) {
        return deserializeCommit(work.getCurrHeadHash());
    }

    /** Implements remove functionality by removing file
     * FILENAME from Reference files that are tracked by commit.
     * @param fileName
     */
    public static void rm(String fileName) {
        Work work = deserializeWork();
        Commit head = getCurrHeadCommit(work);
        StagingArea stage = work.getStagingArea();
        ArrayList<String> additionArea = stage.getAdditionArea();
        if (stage.filteredAddition(fileName) == null
                && stage.filteredRemoval(fileName) == null
                && head.filteredReferenceFile(fileName) == null) {
            System.out.println("No reason to remove the file");
            return;
        }
        stage.cancelStagedAddition(fileName);
        if (head.filteredReferenceFile(fileName) != null) {
            stage.manualAddRemoval(fileName);
            if (Utils.join(CWD, fileName).exists()) {
                stage.stageForRemoval(fileName);
                Utils.restrictedDelete(Utils.join(CWD, fileName));
            }
        }
        serializeWork(work);
    }

    /** Implements gitlet log functionality.
     *   display information about each commit backwards
     *   along the commit tree until the initial commit,
     *   following the first parent commit links,
     *   ignoring any second parents found in merge commits.
     */
    public static void log() {
        Work work = deserializeWork();
        Commit currentCommit = getCurrHeadCommit(work);
        ArrayList<Commit> currBranchCommitHistory =
                work.getCurrBranchCommitHistory();
        SimpleDateFormat dt = new SimpleDateFormat(
                "EEE MMM d hh:mm:ss yyyy Z", Locale.ENGLISH);
        while (currentCommit != null) {
            String logString = "===\ncommit " + getCommitHash(currentCommit);
            if (currentCommit.isMerge()) {
                logString += "\nMerge: "
                        + currentCommit.getPrevHash(0).substring(0, 7)
                        + " " + currentCommit.getPrevHash(1).substring(0, 7);
            }
            logString += "\nDate: " + dt.format(currentCommit.getTimestamp())
                    + "\n" + currentCommit.getCommitMessage() + "\n";
            System.out.println(logString);
            if (currentCommit.getCommitMessage()
                    .equals("initial commit")) {
                break;
            }
            String hash = currentCommit.getPrevHash(0);
            currentCommit = deserializeCommit(hash);
        }
    }

    /** Implements the global-log functionality of gitlet.
     * Displays information about all commits ever made
     * in no particular order.
     */
    private static void globalLog() {
        List<String> commits = Utils.plainFilenamesIn(COMMIT_FOLDER);
        SimpleDateFormat dt = new SimpleDateFormat(
                "EEE MMM d hh:mm:ss yyyy Z", Locale.ENGLISH);
        for (String commitHash: commits) {
            Commit currentCommit = deserializeCommit(commitHash);
            String logString = "===\ncommit " + getCommitHash(currentCommit);
            if (currentCommit.isMerge()) {
                logString += "\nMerge: "
                        + currentCommit.getPrevHash(0).substring(0, 7)
                        + " " + currentCommit.getPrevHash(1).substring(0, 7);
            }
            logString += "\nDate: " + dt.format(currentCommit.getTimestamp())
                    + "\n" + currentCommit.getCommitMessage() + "\n";
            System.out.println(logString);
        }
    }

    /** Implements find functionlity of gitlet.
     * Prints out the ids of all commits that have
     * the given COMMITMESSAGE.
     * @param commitMessage
     */
    private static void find(String commitMessage) {
        List<String> commits = Utils.plainFilenamesIn(COMMIT_FOLDER);
        int count = 0;
        for (String commitHash: commits) {
            Commit currentCommit = deserializeCommit(commitHash);
            String message = currentCommit.getCommitMessage();
            if (message.equals(commitMessage)) {
                count += 1;
                System.out.println(commitHash);
            }
        }
        if (count == 0) {
            System.out.println("Found no commit with that message.");
            return;
        }
    }

    /** Implements gitlet checkout functionality.
     * Checks out FILENAME.
     * @param fileName
     */
    private static void checkout(String fileName) {
        Work work = deserializeWork();
        Commit head = getCurrHeadCommit(work);
        filteredCheckout(head, fileName);
    }

    /** Find full commit id given ABBREV.
     * @param abbrev
     * @return
     */
    private static String getFullCommitId(String abbrev) {
        List<String> fullCommitID = Utils.plainFilenamesIn(COMMIT_FOLDER);
        String completeCommitID = null;
        for (String fullID: fullCommitID) {
            if (fullID.substring(0, abbrev.length()).equals(abbrev)) {
                completeCommitID = fullID;
                break;
            }
        }
        return  completeCommitID;
    }

    /** Implements gitlet checkout functionality.
     *  Checksout FILENAME in commit COMMITID id exists.
     * @param commitID
     * @param fileName
     */
    private static void checkout(String commitID, String fileName) {
        String completeCommitID = getFullCommitId(commitID);
        if (completeCommitID == null) {
            System.out.println("No commit with that id exists.");
            return;
        }
        Commit commit = deserializeCommit(completeCommitID);
        filteredCheckout(commit, fileName);
    }

    /** Overwrite file FILENAME in working folder with F.
     * @param f
     */
    private static void overwriteWorking(FileReference f) {
        Utils.writeContents(Utils.join(Main.CWD, f.getFileName()),
                Utils.readContentsAsString(
                        deserializeBlob(f.getReferenceHash())));
    }

    /** Help loop through COMMIT FILENAME.
     * @param commit
     * @param fileName
     */
    private static void filteredCheckout(Commit commit, String fileName) {
        Work work = deserializeWork();
        StagingArea stage = work.getStagingArea();
        ArrayList<FileReference> tracked = commit.getReferenceFiles();
        if (commit.filteredReferenceFile(fileName) == null) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        for (FileReference f: tracked) {
            if (f.getFileName().equals(fileName)) {
                overwriteWorking(f);
                break;
            }
        }
        stage.cancelStagedAddition(fileName);
        serializeWork(work);
    }

    /** Delete files in CWD corresponding to F.
     * @param f
     */
    private static void deleteWorking(FileReference f) {
        if (Utils.join(Main.CWD, f.getFileName()).exists()) {
            Utils.restrictedDelete(Utils.join(Main.CWD, f.getFileName()));
        }
    }

    /** Check if there is an untracked file
     * in the current head commit CURR
     * with respect to PREV.
     * @param prev
     * @param curr
     * @return
     */
    private static boolean isDiffTracked(Commit prev, Commit curr) {
        boolean diff = false;
        ArrayList<String> prevRefNames = prev.getAllRefNames();
        ArrayList<String> currRefNames = curr.getAllRefNames();
        for (String prevName: prevRefNames) {
            if (Utils.join(Main.CWD, prevName).exists()) {
                int search = 0;
                if (curr.filteredReferenceFile(prevName) != null) {
                    search += 1;
                    break;
                }
                if (search == 0) {
                    System.out.println("There is an untracked "
                            + "file in the way; "
                            + "delete it, or add and commit it first.");
                    return !diff;
                }
            }
        }
        return diff;
    }

    /** Check if the given branch exists, and whether it is the
     * current branch BRANCHNAME MESSAGE1 MESSAGE2.
     * @param branchName
     * @param message
     * @return
     */
    public static boolean checkBranchExceptions(
            String branchName, String message1, String message2) {
        boolean error = false;
        Work work = deserializeWork();
        if (work.filteredBranch(branchName) == null) {
            System.out.println(message1);
            return !error;
        } else if (work.getCurrBranch().equals(branchName)) {
            System.out.println(message2);
            return !error;
        }
        return error;
    }

    /** Implements gitlet checkout functionality. Checks out BRANCHNAME.
     * @param branchName
     */
    private static void checkoutBranch(String branchName) {
        String message1 = "No such branch exists.";
        String message2 = "No need to checkout the current branch.";
        if (checkBranchExceptions(branchName, message1, message2)) {
            return;
        }
        Work work = deserializeWork();
        Commit head = getCurrHeadCommit(work);
        ArrayList<FileReference> tracked = head.getReferenceFiles();
        String branchHash = work.getHeadHash(branchName);
        Commit branchHead = deserializeCommit(branchHash);
        ArrayList<FileReference> branchHeadTracked =
                branchHead.getReferenceFiles();
        isDiffTracked(branchHead, head);
        for (FileReference currBlob: tracked) {
            deleteWorking(currBlob);
        }
        for (FileReference branchBlob: branchHeadTracked) {
            overwriteWorking(branchBlob);
        }
        work.getStagingArea().deleteAllStage();
        work.setCurrBranch(branchName);
        work.setCurrHeadHash(branchHash);
        serializeWork(work);
    }

    /** Gitlet status functionality.
     * Displays what branches currently exist,
     * and marks the current branch with a *.
     * Also displays what files have been staged for addition or removal.
     */
    private static void status() {
        Work work = deserializeWork();
        branchStatus(work);
        additionStatus(work);
        removalStatus(work);
        modifiedStatus(work);
        untrackedStatus(work);
    }

    /** Prints out status of branches WORK.
     * @param work
     */
    private static void branchStatus(Work work) {
        ArrayList<CommitBranch> branches = work.getBranches();
        String[] branchHolder = new String[branches.size()];
        int index = 0;
        for (CommitBranch b: branches) {
            branchHolder[index] = b.getBranchName();
            index += 1;
        }
        Arrays.sort(branchHolder, String.CASE_INSENSITIVE_ORDER);
        System.out.println("=== Branches ===");
        for (String b: branchHolder) {
            if (b.equals(work.getCurrBranch())) {
                System.out.print("*");
            }
            System.out.println(b);
        }
        System.out.print("\n");
    }

    /** Prints out status of addition area
     * (staged files) WORK.
     * @param work
     */
    private static void additionStatus(Work work) {
        stageStatus("Staged Files", work);
    }

    /** Prints out status of removal area
     * (files that are staged for removal) WORK.
     * @param work
     */
    private static void removalStatus(Work work) {
        stageStatus("Removed Files", work);
    }

    /** Sorts files in the staging area and prints them out
     * PLACE WORK.
     *  PLACE AND WORK.
     * @param place
     * @param work
     */
    private static void stageStatus(String place, Work work) {
        ArrayList<String> area = null;
        if (place.equals("Staged Files")) {
            area = work.getStagingArea().getAdditionArea();
        } else {
            area = work.getStagingArea().getRemovalArea();
        }
        String[] stagedFiles = new String[area.size()];
        int index = 0;
        for (String stagedFile: area) {
            stagedFiles[index] = stagedFile;
            index += 1;
        }
        Arrays.sort(stagedFiles, String.CASE_INSENSITIVE_ORDER);
        System.out.println("=== " + place + " ===");
        for (String stagedFile: stagedFiles) {
            System.out.println(stagedFile);
        }
        System.out.print("\n");
    }

    /** Sorts names of file that are, according to definition on the spec,
     * modified WORK.
     * @param work
     */
    private static void modifiedStatus(Work work) {
        Commit head = getCurrHeadCommit(work);
        ArrayList<FileReference> ref = head.getReferenceFiles();
        StagingArea stage = work.getStagingArea();
        ArrayList<String> additionArea = stage.getAdditionArea();
        ArrayList<String> modified = new ArrayList<>();
        for (FileReference r: ref) {
            String rFileName = r.getFileName();
            if (!Utils.join(CWD, rFileName).exists()) {
                if (stage.filteredRemoval(rFileName) == null) {
                    modified.add(rFileName + " (deleted)");
                    continue;
                }
            } else if (stage.filteredAddition(r.getFileName()) == null
                    && stage.filteredRemoval(r.getFileName()) == null) {
                String hash = getFileHash(Utils.join(CWD, rFileName));
                if (!r.getReferenceHash().equals(hash)) {
                    modified.add(rFileName + " (modified)");
                }
            }
        }
        for (String add: additionArea) {
            File staged = stage.getStagedAdditionFile(add);
            File workFile = Utils.join(CWD, add);
            if (!workFile.exists()) {
                modified.add(add + " (deleted)");
                continue;
            }
            String hash = getFileHash(workFile);
            if (!getFileHash(staged).equals(hash)) {
                modified.add(add + " (modified)");
            }
        }
        Collections.sort(modified);
        System.out.println("=== "
                + "Modifications Not Staged For Commit ===");
        for (String mod: modified) {
            System.out.println(mod);
        }
        System.out.print("\n");
    }

    /** Sorts files that are untracked and print them out WORK.
     * @param work
     */
    private static void untrackedStatus(Work work) {
        Commit head = getCurrHeadCommit(work);
        StagingArea stage = work.getStagingArea();
        List<String> workFiles = Utils.plainFilenamesIn(CWD);
        ArrayList<String> untracked = new ArrayList<>();
        for (String w: workFiles) {
            if (head.filteredReferenceFile(w) == null
                    && stage.filteredAddition(w) == null) {
                untracked.add(w);
            }
        }
        Collections.sort(untracked);
        System.out.println("=== Untracked Files ===");
        for (String free: untracked) {
            System.out.println(free);
        }
    }

    /**  Creates a new branch with the given name,
     * and points it at the currentheadnode
     * BRANCHNAME.
     * @param branchName*/
    private static void branch(String branchName) {
        Work work = deserializeWork();
        if (work.filteredBranch(branchName) != null) {
            System.out.println("A branch with that name already exists.");
            return;
        }
        work.addBranchHash(branchName, getCommitHash(getCurrHeadCommit(work)));
        serializeWork(work);
    }

    /** Deletes the branch with the given name.
     * This only means to delete the pointer associated with the branch;
     * it does not mean to delete all commits
     * that were created under the branch,
     * or anything like that BRANCHNAME.
     * @param branchName*/
    private static void rmBranch(String branchName) {
        String message1 = "A branch with that name does not exist.";
        String message2 = "Cannot remove the current branch.";
        checkBranchExceptions(branchName, message1, message2);
        Work work = deserializeWork();
        work.removeBranch(branchName);
        serializeWork(work);
    }

    /** Checks out all the files tracked by the given commit.
     * Removes tracked files that are not present in that commit.
     * Also moves the current branch's head to that commit node
     * COMMITID.
     * @param commitHash*/
    private static void reset(String commitId) {
        Work work = deserializeWork();
        Commit head = getCurrHeadCommit(work);
        ArrayList<FileReference> currTracked = head.getReferenceFiles();
        ArrayList<String> currRefNames = head.getAllRefNames();
        String fullId = getFullCommitId(commitId);
        if (fullId == null) {
            System.out.println("No commit with that id exists.");
            return;
        }
        Commit prev = deserializeCommit(fullId);
        ArrayList<FileReference> prevTracked = prev.getReferenceFiles();
        ArrayList<String> prevRefNames = prev.getAllRefNames();
        if (isDiffTracked(prev, head)) {
            return;
        }
        for (FileReference currBlob: currTracked) {
            deleteWorking(currBlob);
        }
        for (FileReference prevBlob: prevTracked) {
            overwriteWorking(prevBlob);
        }
        for (String currName: currRefNames) {
            for (String prevNames: prevRefNames) {
                if (prev.filteredReferenceFile(currName) == null) {
                    head.removeReferenceFile(currName);
                }
            }
        }
        work.setCurrHeadHash(fullId);
        work.setCurrBranchHash(fullId);
        work.getStagingArea().deleteAllStage();
        serializeWork(work);
    }

    /** Find the split point of divergence of two branches
     * to be merged BRANCHNAME.
     * @param branchName
     * @return
     */
    public static String findSplitPoint(String branchName) {
        Work work = deserializeWork();
        String splitPoint = null;
        ArrayDeque<String> workQueue = new ArrayDeque<>();
        ArrayList<String> branchCommits = new ArrayList<>();
        String headCommit = getCommitHash(getCurrHeadCommit(work));
        String branchCommit = work.getHeadHash(branchName);
        workQueue.addLast(branchCommit);
        while (workQueue.size() > 0) {
            String commit = workQueue.removeFirst();
            branchCommits.add(commit);
            Commit commitObj = deserializeCommit(commit);
            LinkedList<String> prev = commitObj.getAllParentCommit();
            for (String parent: prev) {
                workQueue.addLast(parent);
            }
        }
        workQueue.addLast(headCommit);
        while (workQueue.size() > 0) {
            String commit = workQueue.removeLast();
            if (branchCommits.contains(commit)) {
                splitPoint = commit;
                break;
            } else {
                Commit commitObj = deserializeCommit(commit);
                LinkedList<String> prev = commitObj.getAllParentCommit();
                for (String parent: prev) {
                    workQueue.addLast(parent);
                }
            }
        }
        return splitPoint;
    }

    /** Check failure cases for merge WORK
     * BRANCHNAME.
     * @param branchName
     * @return
     */
    public static boolean checkMergeExceptions(Work work, String branchName) {
        boolean error = false;
        String message1 = "A branch with that name does not exist.";
        String message2 = "Cannot merge a branch with itself.";
        if (checkBranchExceptions(branchName, message1, message2)) {
            return !error;
        }
        StagingArea stage = work.getStagingArea();
        if (stage.getAdditionArea().size() > 0
                || stage.getRemovalArea().size() > 0) {
            System.out.println("You have uncommitted changes.");
            return !error;
        }
        String branchHeadHash = work.getHeadHash(branchName);
        Commit currHead = getCurrHeadCommit(work);
        String currHeadHash = getCommitHash(currHead);
        isDiffTracked(deserializeCommit(branchHeadHash),
                currHead);
        String splitPoint = findSplitPoint(branchName);
        if (splitPoint.equals(branchHeadHash)) {
            System.out.println("Given branch is an ancestor of "
                    + "the current branch.");
            return !error;
        } else if (splitPoint.equals(currHeadHash)) {
            String currBranch = work.getCurrBranch();
            checkoutBranch(branchName);
            work = deserializeWork();
            work.setCurrBranch(currBranch);
            serializeWork(work);
            System.out.println("Current branch fast-forwarded.");
            return !error;
        }
        return error;
    }

    /** Commit merged files given by merge command
     * WORK BRANCHNAME PREVHASH CURRNAME CURRHASH.
     * @param branchName
     * @param prevHash
     * @param currName
     * @param currHash
     */
    public static void mergeCommit(Work work,
                                   String branchName, String prevHash,
                                   String currName, String currHash) {
        Commit currHead = getCurrHeadCommit(work);
        ArrayList<FileReference> currTracked = currHead.getReferenceFiles();
        Commit merged = new Commit("Merged " + branchName + " into "
                + currName + ".");
        merged.addPrevHash(currHash);
        merged.addPrevHash(prevHash);
        merged.setReferenceFiles(currTracked);
        StagingArea stage = work.getStagingArea();
        ArrayList<String> additionArea = stage.getAdditionArea();
        ArrayList<String> removalArea = stage.getRemovalArea();
        for (String fileName: additionArea) {
            File stagedFile = stage.getStagedAdditionFile(fileName);
            String fileHash = getFileHash(stagedFile);
            FileReference oldVersion = merged.filteredReferenceFile(fileName);
            if (oldVersion != null) {
                oldVersion.updateVersion();
            }
            merged.addReferenceFiles(stagedFile, fileName, fileHash);
            serializeBlob(stagedFile);
        }
        for (String fileName: removalArea) {
            merged.removeReferenceFile(fileName);
            Utils.restrictedDelete(Utils.join(CWD, fileName));
        }
        stage.deleteAllStage();
        work.updateCurrBranchCommitHistory(merged);
        serializeCommit(merged);
        work.setCurrHeadHash(getCommitHash(merged));
        work.addBranchHash(work.getCurrBranch(), getCommitHash(merged));
    }

    /** Lists all possible states of a merge
     * and apply actions needed
     * repectively WORK ALLFILENAMES SPLIT SPLITPOINT
     * HEAD HEADHASH BRANCHHEAD BRANCHHASH.
     * @param work
     * @param allFileNames
     * @param split
     * @param splitPoint
     * @param head
     * @param headHash
     * @param branchHead
     * @param branchHash
     */
    public static void mergeCases(Work work, ArrayList<String> allFileNames,
                                  Commit split, String splitPoint,
                                  Commit head, String headHash,
                                  Commit branchHead, String branchHash) {
        StagingArea stage = work.getStagingArea();
        for (String fileName: allFileNames) {
            if (_usedNames.contains(fileName)) {
                continue;
            }
            _usedNames.add(fileName);
            FileReference splitRef = split.filteredReferenceFile(fileName);
            FileReference headRef = head.filteredReferenceFile(fileName);
            FileReference brHRef = branchHead.filteredReferenceFile(fileName);
            if (splitRef != null && headRef != null && brHRef != null
                    && headRef.equals(splitRef) && !brHRef.equals(splitRef)) {
                mergeAction(work, stage, "checkout",
                        branchHash, brHRef, headRef);
            } else if (splitRef != null && headRef != null && brHRef != null
                    && !headRef.equals(splitRef) && brHRef.equals(splitRef)) {
                continue;
            } else if (splitRef != null && headRef == null && brHRef == null) {
                continue;
            } else if (splitRef != null && headRef != null && brHRef == null
                    && headRef.equals(splitRef)) {
                mergeAction(work, stage, "remove",
                        branchHash, brHRef, headRef);
            } else if (splitRef != null && headRef == null && brHRef != null
                    && splitRef.equals(brHRef)) {
                continue;
            } else if (splitRef != null && headRef != null && brHRef != null
                    && !headRef.equals(splitRef) && !brHRef.equals(splitRef)
                    && headRef.equals(brHRef)) {
                continue;
            } else if (splitRef == null && headRef == null && brHRef != null) {
                mergeAction(work, stage, "checkout",
                        branchHash, brHRef, headRef);
            } else if (splitRef == null && headRef != null && brHRef == null) {
                continue;
            } else if (splitRef != null && headRef != null && brHRef != null
                    && headRef.equals(splitRef) && brHRef.equals(headRef)) {
                continue;
            } else if (splitRef != null && headRef != null && brHRef != null
                    && !headRef.equals(splitRef) && !brHRef.equals(splitRef)
                    && !headRef.equals(brHRef)) {
                mergeAction(work, stage, "conflict",
                        branchHash, brHRef, headRef);
            } else if (splitRef != null && headRef != null && brHRef == null
                    && !headRef.equals(splitRef)) {
                mergeAction(work, stage, "conflict",
                        branchHash, brHRef, headRef);
            } else if (splitRef != null && brHRef != null
                    && headRef == null && !brHRef.equals(splitRef)) {
                mergeAction(work, stage, "conflict",
                        branchHash, brHRef, headRef);
            } else if (splitRef == null && headRef != null && brHRef != null
                    && !headRef.equals(brHRef)) {
                mergeAction(work, stage, "conflict",
                        branchHash, brHRef, headRef);
            }
        }
        serializeWork(work);
    }

    /** Apply merge actions to different cases
     * determined by mergeCases
     * WORK STAGE ACTION BRANCHHASH BRANCHHEADREF
     * HEADREF.
     * @param stage
     * @param action
     * @param branchHash
     * @param branchHeadRef
     * @param headRef
     */
    public static void mergeAction(Work work,
                                   StagingArea stage,
                                   String action,
                                   String branchHash,
                                   FileReference branchHeadRef,
                                   FileReference headRef) {
        if (action.equals("checkout")) {
            checkout(branchHash, branchHeadRef.getFileName());
            stage.stageForAddition(branchHeadRef.getFileName());
        } else if (action.equals("remove")) {
            stage.stageForRemoval(headRef.getFileName());
        } else if (action.equals("conflict")) {
            String ret = "<<<<<<< HEAD\n";
            String headContent = "";
            if (headRef != null) {
                headContent = Utils.readContentsAsString(
                        deserializeBlob(headRef.getReferenceHash()));
            }
            String branchHeadContent = "";
            if (branchHeadRef != null) {
                branchHeadContent = Utils.readContentsAsString(
                        deserializeBlob(branchHeadRef.getReferenceHash()));
            }
            ret += headContent + "=======\n" + branchHeadContent + ">>>>>>>\n";
            Utils.writeContents(new File(headRef.getFileName()), ret);
            stage.stageForAddition(headRef.getFileName());
            System.out.println("Encountered a merge conflict.");
        }
    }

    /** Gitlet's merge functionality, which merges a branch
     * with the current branch BRANCHNAME.
     * @param branchName
     */
    public static void merge(String branchName) {
        Work work = deserializeWork();
        if (checkMergeExceptions(work, branchName)) {
            return;
        }
        String splitPoint = findSplitPoint(branchName);
        Commit split = deserializeCommit(splitPoint);
        ArrayList<FileReference> splitTracked = split.getReferenceFiles();
        Commit head = getCurrHeadCommit(work);
        ArrayList<FileReference> headTracked = head.getReferenceFiles();
        String branchHash = work.getHeadHash(branchName);
        Commit branchHead = deserializeCommit(branchHash);
        ArrayList<FileReference> branchHeadTracked =
                branchHead.getReferenceFiles();
        ArrayList<String> allFileNames = new ArrayList<>();
        for (FileReference f: splitTracked) {
            allFileNames.add(f.getFileName());
        }
        for (FileReference f: headTracked) {
            allFileNames.add(f.getFileName());
        }
        for (FileReference f: branchHeadTracked) {
            allFileNames.add(f.getFileName());
        }
        _usedNames = new ArrayList<>();
        mergeCases(work, allFileNames, split, splitPoint, head,
                work.getCurrHeadHash(), branchHead, branchHash);
        mergeCommit(work, branchName, branchHash,
                work.getCurrBranch(), work.getCurrHeadHash());
        serializeWork(work);
    }

    /** Saves the given login information under the given remote name.
     * Attempts to push or pull from the given remote name will then
     * attempt to use this .gitlet directory
     * REMOTENAME REMOTEDIR.
     * @param remoteName
     * @param remoteDir
     */
    public static void addRemote(String remoteName, String remoteDir) {
        Work work = deserializeWork();
        work.getRemote().addRemoteDir(remoteName, remoteDir);
        serializeWork(work);
    }

    /** Remove information associated with the given remote name.
     * The idea here is that if you ever wanted to
     * change a remote that you added,
     * you would have to first remove it and then re-add it
     * REMOTENAME.
     * @param remoteName
     */
    public static void rmRemote(String remoteName) {
        Work work = deserializeWork();
        work.getRemote().removeRemoteDir(remoteName);
        serializeWork(work);
    }

    /** Saves changes to remote work object REMOTEDIR
     * REMOTEWORK.
     * @param remoteDir
     * @param remoteWork
     */
    private static void serializeRemoteWork(File remoteDir, Work remoteWork) {
        Utils.writeObject(Utils.join(remoteDir, "work"), remoteDir);
    }

    /** Get the remote work object REMOTEDIR.
     * @param remoteDir
     * @return
     */
    public static Work deserializeRemoteWork(File remoteDir) {
        return Utils.readObject(Utils.join(remoteDir, "work"), Work.class);
    }

    /** Get the remote commit object
     * REMOTEDIR COMMITHASH.
     * @param remoteDir
     * @param commitHash
     * @return
     */
    public static Commit deserializeRemoteCommit(File remoteDir,
                                                 String commitHash) {
        return Utils.readObject(Utils.join(
                Utils.join(remoteDir, "commits"), commitHash), Commit.class);
    }

    /** Attempts to append the current branch's commits
     * to the end of the given branch at the given remote
     * REMOTENAME REMOTEBRANCHNAME.
     * @param remoteName
     * @param remoteBranchName
     */
    public static void push(String remoteName, String remoteBranchName) {
        Work work = deserializeWork();
        File remoteDir = work.getRemote().getRemoteDir(remoteName);
        if (!remoteDir.exists()) {
            System.out.println("Remote directory not found.");
            return;
        }
        boolean inHistory = false;
        Commit head = getCurrHeadCommit(work);
        String headHash = getCommitHash(head);
        Work remoteWork = deserializeRemoteWork(remoteDir);
        String remoteBranchHeadHash = remoteWork.getHeadHash(remoteBranchName);
        while (head != null) {
            if (headHash.equals(remoteBranchHeadHash)) {
                inHistory = true;
                break;
            }
            if (head.getCommitMessage()
                    .equals("initial commit")) {
                break;
            }
            String hash = head.getPrevHash(0);
            head = deserializeCommit(hash);
        }
        if (!inHistory) {
            System.out.println(
                    "Please pull down remote changes before pushing.");
        }
        Commit headCopy = getCurrHeadCommit(work);
        String headCopyHash = getCommitHash(headCopy);
        List<String> blobs = Utils.plainFilenamesIn(BLOB_FOLDER);
        for (String blob: blobs) {
            overwriteRemoteBlob(remoteDir, blob);
        }
        while (!headCopyHash.equals(remoteBranchHeadHash)) {
            overwriteRemoteCommit(remoteDir, headCopyHash);
            if (head.getCommitMessage()
                    .equals("initial commit")) {
                break;
            }
            headCopyHash = headCopy.getPrevHash(0);
            headCopy = deserializeCommit(headCopyHash);
        }
        remoteWork.addBranchHash(remoteBranchName, headHash);
        if (remoteBranchName.equals(MASTER_BRANCH)) {
            remoteWork.setCurrHeadHash(headHash);
        }
        serializeRemoteWork(remoteDir, remoteWork);
        serializeWork(work);
    }

    /** Brings down commits from the remote Gitlet repository
     * into the local Gitlet repository
     * REMOTENAME REMOTEBRANCHNAME.
     * @param remoteName
     * @param remoteBranchName
     */
    public static void fetch(String remoteName, String remoteBranchName) {
        Work work = deserializeWork();
        File remoteDir = work.getRemote().getRemoteDir(remoteName);
        if (!remoteDir.exists()) {
            System.out.println("Remote directory not found.");
            return;
        }
        Work remoteWork = deserializeRemoteWork(remoteDir);
        if (remoteWork.filteredBranch(remoteBranchName) == null) {
            System.out.println("The remote does not have that branch.");
        }
        String remoteBranchHeadHash = remoteWork.getHeadHash(remoteBranchName);
        Commit remoteBranchHead =
                deserializeRemoteCommit(remoteDir, remoteBranchHeadHash);
        List<String> remoteblobs =
                Utils.plainFilenamesIn(Utils.join(remoteDir, "blobs"));
        for (String blob: remoteblobs) {
            overwriteBlob(remoteDir, blob);
        }
        Commit headCopy = getCurrHeadCommit(work);
        String headCopyHash = getCommitHash(headCopy);
        while (!remoteBranchHeadHash.equals(headCopyHash)) {
            overwriteCommit(remoteDir, remoteBranchHeadHash);
            if (remoteBranchHead.getCommitMessage()
                    .equals("initial commit")) {
                break;
            }
            remoteBranchHeadHash = remoteBranchHead.getPrevHash(0);
            remoteBranchHead = deserializeCommit(remoteBranchHeadHash);
        }
        work.addBranchHash("remote " + remoteName + " "
                + remoteBranchName, remoteBranchHeadHash);
        serializeWork(work);
        serializeRemoteWork(remoteDir, remoteWork);
    }

    /** Fetches branch [remote name]/
     * [remote branch name] as for the fetch command,
     * and then merges that fetch into the current branch
     * REMOTENAME REMOTEBRANCHNAME.
     * @param remoteName
     * @param remoteBranchName
     */
    public static void pull(String remoteName, String remoteBranchName) {
        fetch(remoteName, remoteBranchName);
        merge("remote " + remoteName + " " + remoteBranchName);
    }

    /** Overwrite a commit object on this machine
     * REMOTEDIR COMMITHASH.
     * @param remoteDir
     * @param commitHash
     */
    public static void overwriteCommit(File remoteDir, String commitHash) {
        Utils.writeObject(Utils.join(COMMIT_FOLDER, commitHash),
                Utils.readObject(Utils.join(Utils.join(remoteDir, "commits"),
                        commitHash), Commit.class));
    }

    /** Overwrite a blob on this machine
     * REMOTEDIR REF.
     * @param remoteDir
     * @param ref
     */
    public static void overwriteBlob(File remoteDir, String ref) {
        Utils.writeContents(Utils.join(BLOB_FOLDER, ref),
                Utils.readContents(Utils.join(Utils.join(remoteDir, "blobs"),
                        ref)));
    }

    /** Overwrite a blob on a remote machine
     * REMOTEDIR REF.
     * @param remoteDir
     * @param ref
     */
    public static void overwriteRemoteBlob(File remoteDir, String ref) {
        Utils.writeContents(Utils.join(Utils.join(remoteDir, "blobs"),
                        ref),
                Utils.readContents(deserializeBlob(ref)));
    }

    /** Overwrite a remote commit
     * REMOTEDIR COMMITHASH.
     * @param remoteDir
     * @param commitHash
     */
    public static void overwriteRemoteCommit(File remoteDir,
                                             String commitHash) {
        Utils.writeObject(Utils.join(Utils.join(remoteDir, "commits"),
                        commitHash),
                Utils.readObject(
                        Utils.join(COMMIT_FOLDER, commitHash), Commit.class));
    }
}
