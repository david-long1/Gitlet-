package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

/** Going remote.
 * @author David Long */
public class RemoteInfo implements Serializable {

    /** List of file names that are in remote dir. */
    private ArrayList<String> _remotePlace;

    /** Constructor of remoteInfo class. */
    public RemoteInfo() {
        _remotePlace = new ArrayList<>();
    }

    /** Add remote directory. Add REMOTENAME REMOTEDIR.
     * @param remoteName
     * @param remoteDir
     */
    public void addRemoteDir(String remoteName, String remoteDir) {
        if (_remotePlace.contains(remoteName)) {
            System.out.println("A remote with that name already exists.");
            return;
        }
        remoteDir.replaceAll("/", File.separator);
        _remotePlace.add(remoteName);
        Utils.writeContents(Utils.join(Main.REMOTE_FOLDER, remoteName),
                Utils.readContents(new File(remoteDir)));
    }

    /** Remove remoteDir REMOTENAME.
     * @param remoteName
     */
    public void removeRemoteDir(String remoteName) {
        if (!_remotePlace.contains(remoteName)) {
            System.out.println("A remote with that name does not exist.");
            return;
        }
        _remotePlace.remove(remoteName);
        File remoteDir = Utils.join(Main.REMOTE_FOLDER, remoteName);
        remoteDir.delete();
    }

    /** Get REMOTENAME.
     * @param remoteName
     * @return
     */
    public File getRemoteDir(String remoteName) {
        return Utils.join(Main.REMOTE_FOLDER, remoteName);
    }
}
