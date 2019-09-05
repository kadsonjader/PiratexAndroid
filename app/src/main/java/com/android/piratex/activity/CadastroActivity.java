package com.android.piratex.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.piratex.R;
import com.android.piratex.config.ConfiguraçãoFirebase;
import com.android.piratex.helper.UsuarioFirebase;
import com.android.piratex.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class CadastroActivity extends AppCompatActivity {
    private TextInputEditText campoNome;
    private TextInputEditText campoEmail;
    private TextInputEditText campoSenha;
    private Switch switchTipoUsuario;
    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        campoNome = findViewById(R.id.editCadastroNome);
        campoEmail = findViewById(R.id.editCadastroEmail);
        campoSenha = findViewById(R.id.editCadastroSenha);
        switchTipoUsuario = findViewById(R.id.switchTipoUsuario);

    }

    public void validarCadastroUsuario(View view){

        String textoNome = campoNome.getText().toString();
        String textoEmail = campoEmail.getText().toString();
        String textoSenha = campoSenha.getText().toString();

        if(!textoNome.isEmpty()){
            if(!textoEmail.isEmpty()){
                if(!textoSenha.isEmpty()){
                    Usuario usuario = new Usuario();
                    usuario.setNome(textoNome);
                    usuario.setEmail(textoEmail);
                    usuario.setSenha(textoSenha);
                    usuario.setTipo(verificaTipoUsuario());

                    cadastrarUsuario(usuario);

                }else{
                    Toast.makeText(CadastroActivity.this, "Preencha o Senha!", Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(CadastroActivity.this, "Preencha o E-mail!", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(CadastroActivity.this, "Preencha o nome!", Toast.LENGTH_SHORT).show();
        }

    }

    public String verificaTipoUsuario(){

        return switchTipoUsuario.isChecked() ? "M" : "P"  ;
    }

    public void cadastrarUsuario(final Usuario usuario){
        autenticacao = ConfiguraçãoFirebase.getFirebaseAutenticacao();
        autenticacao.createUserWithEmailAndPassword(
                usuario.getEmail(),
                usuario.getSenha()
        ).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        try{

                            String idUsuario = task.getResult().getUser().getUid();
                            usuario.setId(idUsuario);
                            usuario.salvar();

                            UsuarioFirebase.atualizarNomeUsuario(usuario.getNome());

                            if(verificaTipoUsuario() == "P"){
                                startActivity(new Intent(CadastroActivity.this, PassageiroActivity.class));
                                finish();

                                Toast.makeText(CadastroActivity.this,"Sucesso ao cadastrar Passageiro!"
                                        ,Toast.LENGTH_SHORT).show();
                            }else{
                                startActivity(new Intent(CadastroActivity.this, RequisicoesActivity.class));
                                finish();
                                Toast.makeText(CadastroActivity.this,"Sucesso ao cadastrar Motorista!"
                                        ,Toast.LENGTH_SHORT).show();
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }else{
                        String excecao = "";
                        try {
                            throw task.getException();
                        }catch(FirebaseAuthWeakPasswordException e){
                            excecao = "Digite uma senha mais forte !";
                        }catch(FirebaseAuthInvalidCredentialsException e){
                            excecao = "Por favor, digite um e-mail válido";
                        }catch(FirebaseAuthUserCollisionException e){
                            excecao = "Esta conta já foi cadastrada;";
                        }catch(Exception e){
                            excecao = "Erro ao cadastrar usuário: " + e.getMessage();
                            e.printStackTrace();
                        }
                        Toast.makeText(CadastroActivity.this,excecao
                                ,Toast.LENGTH_SHORT).show();
                    }
            }
        });
    }

}
