package io.github.heberfhlemes.githubsearch.ui

import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import io.github.heberfhlemes.githubsearch.R
import io.github.heberfhlemes.githubsearch.data.GitHubService
import io.github.heberfhlemes.githubsearch.domain.Repository
import io.github.heberfhlemes.githubsearch.ui.adapter.RepositoryAdapter
import androidx.core.net.toUri
import androidx.core.content.edit
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val PREF_NAME = "github_usernames_prefs"
private const val KEY_USER = "username"
const val API_BASE_URL = "https://api.github.com/"

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
            nomeUsuario.error = getString(R.string.no_empty_field)
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
        val username = getUsername()

        if (!username.isNullOrEmpty()) {
            nomeUsuario.setText(username)
        }
    }

    /**
     * Retorna o valor relativo ao nome de usuário, armazenado na SharedPreferences.
     */
    private fun getUsername(): String? {
        return getSharedPreferences(PREF_NAME, MODE_PRIVATE)
            .getString(KEY_USER, "")
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
     */
    fun getAllReposByUserName() {
        if (!hasInternet()) {
            showError(R.string.no_internet)
            return
        }

        val username = getUsername().takeUnless { it.isNullOrBlank() } ?: return

        githubApi.getAllRepositoriesByUser(username)
            .enqueue(object : Callback<List<Repository>> {

                override fun onResponse(
                    call: Call<List<Repository>>,
                    response: Response<List<Repository>>
                ) {
                    response.takeIf { it.isSuccessful }
                        ?.body()
                        ?.takeIf { it.isNotEmpty() }
                        ?.let(::setupAdapter)
                        ?: run {
                            showError(R.string.user_no_repos)
                        }
                }

                override fun onFailure(call: Call<List<Repository>>, t: Throwable) {
                    showError(R.string.response_error)
                    Log.e("GitHubAPI", "Erro ao buscar repositórios", t)
                }
            })
    }

    /**
     * Metodo responsável por realizar a configuração do adapter
     */
    fun setupAdapter(list: List<Repository>) {
        val adapter = RepositoryAdapter(list)

        adapter.onItemClick = { repository ->
            openBrowser(repository.htmlUrl)
        }

        adapter.onShareClick = { repository ->
            shareRepositoryLink(repository.htmlUrl)
        }

        listaRepositories.adapter = adapter
    }

    /**
     * Metodo responsável por compartilhar o link do repositório selecionado
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
     */
    fun openBrowser(urlRepository: String) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                urlRepository.toUri()
            )
        )
    }

    /**
     * Verifica se o dispositivo está conectado à Internet para então poder realizar as requisições.
     */
    fun hasInternet(): Boolean {
        val connectivityManager =
            getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork ?: return false
        val capabilities =
            connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }

    /**
     * Chama o [Toast.makeText] para mostrar uma mensagem ao usuário,
     * com o contexto correto desta classe [MainActivity].
     *
     * @param message mensagem a ser apresentada ao usuário
     */
    private fun showError(@StringRes message: Int) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}