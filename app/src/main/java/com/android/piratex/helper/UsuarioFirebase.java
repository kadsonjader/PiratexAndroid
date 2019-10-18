package com.android.piratex.helper;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.android.piratex.activity.PassageiroActivity;
import com.android.piratex.activity.RequisicoesActivity;
import com.android.piratex.config.ConfiguracaoFirebase;
import com.android.piratex.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;

public class UsuarioFirebase {

    public static FirebaseUser getUsuarioAtual(){
        FirebaseAuth usuario = ConfiguracaoFirebase.getFirebaseAutenticacao();
        return usuario.getCurrentUser();
    }

    public static Usuario getDadosUsuarioLogado(){

        FirebaseUser firebaseUser = getUsuarioAtual();

        Usuario usuario = new Usuario();
        usuario.setId( firebaseUser.getUid() );
        usuario.setEmail( firebaseUser.getEmail() );
        usuario.setNome( firebaseUser.getDisplayName() );

        return usuario;

    }

    public static boolean atualizarNomeUsuario(String nome){

        try {

            FirebaseUser user = getUsuarioAtual();
            UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                    .setDisplayName( nome )
                    .build();
            user.updateProfile( profile ).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if( !task.isSuccessful() ){
                        Log.d("Perfil", "Erro ao atualizar nome de perfil.");
                    }
                }
            });

            return true;

        }catch (Exception e){
            e.printStackTrace();
            return false;
        }

    }

    public static void redirecionaUsuarioLogado(final Activity activity){

        FirebaseUser user = getUsuarioAtual();
        if(user != null ){
            Log.d("resultado", "onDataChange: " + getIdentificadorUsuario());
            DatabaseReference usuariosRef = ConfiguracaoFirebase.getFirebaseDatabase()
                    .child("usuarios")
                    .child( getIdentificadorUsuario() );
            usuariosRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d("resultado", "onDataChange: " + dataSnapshot.toString() );
                    Usuario usuario = dataSnapshot.getValue( Usuario.class );

                    String tipoUsuario = usuario.getTipo();
                    if( tipoUsuario.equals("M") ){
                        Intent i = new Intent(activity, RequisicoesActivity.class);
                        activity.startActivity(i);
                    }else {
                        Intent i = new Intent(activity, PassageiroActivity.class);
                        activity.startActivity(i);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }

    public static void atualizarDadosLocalizacao(double lat, double lon){

        //Define nó de local de usuário
        DatabaseReference localUsuario = ConfiguracaoFirebase.getFirebaseDatabase()
                .child("local_usuario");
        GeoFire geoFire = new GeoFire(localUsuario);

        //Recupera dados usuário logado
        Usuario usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();

        //Configura localização do usuário
        geoFire.setLocation(
                usuarioLogado.getId(),
                new GeoLocation(lat, lon),
                new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {
                        if( error != null ){
                            Log.d("Erro", "Erro ao salvar local!");
                        }
                    }
                }
        );

    }

    public static String getIdentificadorUsuario(){
        return getUsuarioAtual().getUid();
    }

}
