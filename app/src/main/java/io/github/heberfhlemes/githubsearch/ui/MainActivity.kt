package io.github.heberfhlemes.githubsearch.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import io.github.heberfhlemes.githubsearch.R
import io.github.heberfhlemes.githubsearch.data.GitHubService
import io.github.heberfhlemes.githubsearch.domain.Repository
import androidx.core.net.toUri
import androidx.core.content.edit
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val PREF_NAME = "github_usernames_prefs"
private const val KEY_USER = "username"
private const val API_BASE_URL = "https://api.github.com/"

class MainActivity : AppCompatActivity() {

    lateinit var nomeUsuario: EditText
    lateinit var btnConfirmar: Button
    lateinit var listaRepositories: RecyclerView
    lateinit var githubApi: GitHubService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupView()
        setupListeners()
        showUserName()
        setupRetrofit()
    }

    /**
     * Metodo responsavel por realizar o setup da view e recuperar os IDs do layout
     */
    fun setupView() {
        nomeUsuario = findViewById(R.id.et_nome_usuario)
        btnConfirmar = findViewById(R.id.btn_confirmar)
        listaRepositories = findViewById(R.id.rv_lista_repositories)
    }

    /**
     * Metodo responsável por configurar os listeners de clique na tela
     */
    private fun setupListeners() {
        btnConfirmar.setOnClickListener {
            saveUserLocal()
            getAllReposByUserName()
        }
    }


    /**
     * Persistir o usuario preenchido no EditText utilizando uma SharedPreferences
     */
    private fun saveUserLocal() {
        val sharedPref = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        if (nomeUsuario.text.isNullOrEmpty()) {
            nomeUsuario.error = "Campo não pode estar vazio"
            return
        }
        val nomeUsuarioValue = nomeUsuario.text.toString()
        sharedPref.edit {
            putString(KEY_USER, nomeUsuarioValue)
        }
    }

    /**
     * Depois de persistir o usuario, exibir sempre as informacoes no EditText.
     * Se a sharedpref possuir algum valor, exibir no proprio editText o valor salvo.
     */
    private fun showUserName() {
        val sharedPref = getSharedPreferences(PREF_NAME, MODE_PRIVATE)

        val username = sharedPref.getString(KEY_USER, "")

        if (!username.isNullOrEmpty()) {
            nomeUsuario.setText(username)
        }
    }

    /**
     * Metodo responsável por fazer a configuração base do Retrofit
     *
     * Documentacao oficial do retrofit - https://square.github.io/retrofit/
     *
     * URL_BASE da API do  GitHub= https://api.github.com/
     *
     * Lembre-se de utilizar o GsonConverterFactory mostrado no curso
    */
    fun setupRetrofit() {
        val retrofit = Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        githubApi = retrofit.create(GitHubService::class.java)
    }

    /**
     * Metodo responsável por buscar todos os repositorios do usuário fornecido
     * @TODO 6 - realizar a implementação do callback do retrofit e chamar o metodo setupAdapter se retornar os dados com sucesso
     */
    fun getAllReposByUserName() {
    }

    /**
     * Metodo responsável por realizar a configuração do adapter
     * @TODO 7 - Implementar a configuracao do Adapter , construir o adapter e instancia-lo passando a listagem dos repositorios
     */
    fun setupAdapter(list: List<Repository>) {
    }

    /**
     * Metodo responsavel por compartilhar o link do repositorio selecionado
     * @Todo 11 - Colocar este metodo no click do share item do adapter
     */
    fun shareRepositoryLink(urlRepository: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, urlRepository)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    /**
     * Metodo responsável por abrir o browser com o link informado do repositório
     * @Todo 12 - Colocar este método no click item do adapter
     */
    fun openBrowser(urlRepository: String) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                urlRepository.toUri()
            )
        )

    }

}