package boaventura.com.devel.br.flutteraudioquery.delegate;

import android.Manifest;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import boaventura.com.devel.br.flutteraudioquery.loaders.AlbumLoader;
import boaventura.com.devel.br.flutteraudioquery.loaders.ArtistLoader;
import boaventura.com.devel.br.flutteraudioquery.loaders.SongLoader;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;


public class AudioQueryDelegate implements PluginRegistry.RequestPermissionsResultListener {
    private static final String ERROR_KEY_PENDING_RESULT = "pending_result";
    private static final String ERROR_KEY_PERMISSION_DENIAL = "permission_denial";

    private static final int REQUEST_CODE_PERMISSION_READ_EXTERNAL = 0x01;
    private static final int REQUEST_CODE_PERMISSION_WRITE_EXTERNAL = 0x02;

    private final PermissionManager m_permissionManager;

    private MethodCall m_pendingCall;
    private MethodChannel.Result m_pendingResult;

    private final ArtistLoader m_artistLoader;
    private final AlbumLoader m_albumLoader;
    private final SongLoader m_songLoader;

    public AudioQueryDelegate(final PluginRegistry.Registrar registrar){

        m_artistLoader = new ArtistLoader(registrar.context());
        m_albumLoader = new AlbumLoader(registrar.context());
        m_songLoader = new SongLoader( registrar.context() );

        m_permissionManager = new PermissionManager() {
            @Override
            public boolean isPermissionGranted(String permissionName) {

                return (ActivityCompat.checkSelfPermission( registrar.activity(), permissionName)
                    == PackageManager.PERMISSION_GRANTED);
            }

            @Override
            public void askForPermission(String permissionName, int requestCode) {
                ActivityCompat.requestPermissions(registrar.activity(), new String[] {permissionName}, requestCode);
            }
        };

        registrar.addRequestPermissionsResultListener(this);
    }

    public void artistSourceHandler(MethodCall call, MethodChannel.Result result){
        if ( canIbeDepedency(call, result)){

            if (m_permissionManager.isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE) ){
                clearPendencies();
                handleReadOnlyMethods(call, result);
            }

            else
                m_permissionManager.askForPermission(Manifest.permission.READ_EXTERNAL_STORAGE,
                        REQUEST_CODE_PERMISSION_READ_EXTERNAL);

        } else finishWithAlreadyActiveError(result);

    }

    public void albumSourceHandler(MethodCall call, MethodChannel.Result result) {
        if ( canIbeDepedency(call, result)){

            if (m_permissionManager.isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE) ){
                clearPendencies();
                handleReadOnlyMethods(call, result);
            }
            else
                m_permissionManager.askForPermission(Manifest.permission.READ_EXTERNAL_STORAGE,
                        REQUEST_CODE_PERMISSION_READ_EXTERNAL);
        } else finishWithAlreadyActiveError(result);
    }

    public void songSourceHandler(MethodCall call, MethodChannel.Result result){
        if ( canIbeDepedency(call, result)){

            if (m_permissionManager.isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE) ){
                clearPendencies();
                handleReadOnlyMethods(call, result);
            }
            else
                m_permissionManager.askForPermission(Manifest.permission.READ_EXTERNAL_STORAGE,
                        REQUEST_CODE_PERMISSION_READ_EXTERNAL);
        } else finishWithAlreadyActiveError(result);
    }

    public void GenreSourceHandler(MethodCall call, MethodChannel.Result result){
        if ( canIbeDepedency(call, result)){

            if (m_permissionManager.isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE) ){
                clearPendencies();
                handleReadOnlyMethods(call, result);
            }

            else
                m_permissionManager.askForPermission(Manifest.permission.READ_EXTERNAL_STORAGE,
                        REQUEST_CODE_PERMISSION_READ_EXTERNAL);
        } else finishWithAlreadyActiveError(result);
    }
    public void playlistSourceHandler(MethodCall call, MethodChannel.Result result){
        // TODO here we'll got two type of methods, read only and write only method
        //

        /* were we'll needd write permissionto create save playlist in future
        if ( canIbeDepedency(call, result, Manifest.permission.READ_EXTERNAL_STORAGE)){
            switch (call.method){

            }
        }*/
    }

    private void handleReadOnlyMethods(MethodCall call, MethodChannel.Result result){
        switch (call.method){
            case "getArtists":
                m_artistLoader.getArtists(result);
                break;
            case "getAlbums":
                m_albumLoader.getAlbums(result);
                break;

            case "getAlbumsFromArtist":
                String artist = call.argument("artist" );
                m_albumLoader.getAlbumsFromArtist(result, artist);
                break;

            case "getSongs":
                m_songLoader.getSongs(result);
                break;

            case "getSongsFromArtist":
                m_songLoader.getSongsFromArtist( result, (String) call.argument("artist" ) );
                break;

            case "getSongsFromAlbum":
                m_songLoader.getSongsFromAlbum( result, (String) call.argument("album_id" ) );
                break;
        }

    }

    private void handleWriteOnlyMethods(MethodCall call, MethodChannel.Result result){
        result.notImplemented();
    }


    private boolean canIbeDepedency(MethodCall call, MethodChannel.Result result){

        if ( !setPendingMethodAndCall(call, result) ){
            return false;
        }
        return true;
    }

    private boolean setPendingMethodAndCall(MethodCall call, MethodChannel.Result result){
        //There is something that needs to be delivered...
        if (m_pendingResult != null)
            return false;

        m_pendingCall = call;
        m_pendingResult = result;
        return true;
    }

    private void clearPendencies(){
        m_pendingResult = null;
        m_pendingCall = null;
    }

    private void finishWithAlreadyActiveError(MethodChannel.Result result){
        result.error(ERROR_KEY_PENDING_RESULT,
                "There is some result to be delivered", null);
    }

    private void finishWithError(String errorKey, String errorMsg, MethodChannel.Result result){
        result.error(errorKey, errorMsg, null);
        clearPendencies();
    }

    @Override
    public boolean onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        boolean permissionGranted = grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED;

        switch (requestCode){

            case REQUEST_CODE_PERMISSION_READ_EXTERNAL:
                if (permissionGranted){
                    handleReadOnlyMethods(m_pendingCall, m_pendingResult);
                    clearPendencies();
                }
                else {
                    finishWithError(ERROR_KEY_PERMISSION_DENIAL,
                            "READ EXTERNAL PERMISSION DENIED", m_pendingResult);
                }
                break;


            case REQUEST_CODE_PERMISSION_WRITE_EXTERNAL:
                if (permissionGranted){
                    handleWriteOnlyMethods(m_pendingCall, m_pendingResult);
                    clearPendencies();
                }

                else {
                    finishWithError(ERROR_KEY_PERMISSION_DENIAL,
                            "WRITE EXTERNAL PERMISSION DENIED", m_pendingResult);
                }
                break;

            default:
                return false;
        }

        return true;
    }

    interface PermissionManager {
        boolean isPermissionGranted(String permissionName);
        void askForPermission(String permissionName, int requestCode);
    }
}