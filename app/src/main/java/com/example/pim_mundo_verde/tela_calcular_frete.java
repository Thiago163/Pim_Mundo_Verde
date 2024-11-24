package com.example.pim_mundo_verde;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

public class tela_calcular_frete extends AppCompatActivity {

    private EditText cepEditText, logradouroEditText, localidadeEditText, bairroEditText, ufEditText, cepDestino;
    private Button buscarButton, avancarButton, calcularFreteButton;
    private TextView resultadoFrete;
    private TextInputEditText pesoProdutoEditText; // Campo para exibir o peso do produto
    private boolean isFormComplete = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_calcular_frete);

        // Inicializa os componentes de UI
        cepEditText = findViewById(R.id.cepOrigem);
        logradouroEditText = findViewById(R.id.logradouro);
        localidadeEditText = findViewById(R.id.localidade);
        bairroEditText = findViewById(R.id.bairro);
        ufEditText = findViewById(R.id.ufEditText);
        pesoProdutoEditText = findViewById(R.id.pesoProduto);  // Referência para o campo de peso
        buscarButton = findViewById(R.id.botaoBuscar);
        cepDestino = findViewById(R.id.cepDestino);  // Inicializa o campo cepDestino
        avancarButton = findViewById(R.id.botaoAvancar);
        resultadoFrete = findViewById(R.id.resultadoFrete);
        calcularFreteButton = findViewById(R.id.botaoCalcularFrete); // Inicializando o botão de calcular frete

        // Recupera a quantidade de produtos passada da tela anterior
        int quantidadeProdutos = getIntent().getIntExtra("quantidadeProdutos", 0);  // Valor default é 0 se não for passado

        // Agora, você pode usar a quantidade de produtos, que é igual a 1 kg por produto
        float pesoTotal = quantidadeProdutos * 1.0f;  // Cada produto equivale a 1 kg, então multiplicamos pela quantidade

        // Exibe o peso total no campo de texto, formatado com duas casas decimais
        pesoProdutoEditText.setText(String.format("%.2f", pesoTotal));

        // Configura os listeners para os botões
        buscarButton.setOnClickListener(v -> buscarCep());
        avancarButton.setOnClickListener(v -> avancarParaProximaTela());
        calcularFreteButton.setOnClickListener(v -> calcularFrete()); // Definindo a ação para o cálculo do frete
    }

    private void buscarCep() {
        String cep = cepEditText.getText().toString().trim();
        if (cep.isEmpty()) {
            Toast.makeText(this, "Por favor, insira um CEP válido", Toast.LENGTH_SHORT).show();
            return;
        }

        // Construa a URL para a API do ViaCEP
        String url = "https://viacep.com.br/ws/" + cep + "/json/";

        // Utiliza Volley para buscar os dados do CEP
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Preenche os campos com os dados retornados pela API
                            String logradouro = response.optString("logradouro");
                            String bairro = response.optString("bairro");
                            String localidade = response.optString("localidade");
                            String uf = response.optString("uf");

                            logradouroEditText.setText(logradouro);
                            bairroEditText.setText(bairro);
                            localidadeEditText.setText(localidade);
                            ufEditText.setText(uf);  // Preenche o campo UF com o valor retornado

                            // Verifica se todos os campos obrigatórios estão preenchidos
                            verificarCamposPreenchidos();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(tela_calcular_frete.this, "Erro ao buscar CEP", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(tela_calcular_frete.this, "Erro ao consultar o CEP", Toast.LENGTH_SHORT).show();
                    }
                });

        queue.add(jsonObjectRequest);
    }

    private void verificarCamposPreenchidos() {
        String logradouro = logradouroEditText.getText().toString().trim();
        String localidade = localidadeEditText.getText().toString().trim();
        String bairro = bairroEditText.getText().toString().trim();
        String uf = ufEditText.getText().toString().trim();  // Verifica se o campo UF está preenchido

        if (!logradouro.isEmpty() && !localidade.isEmpty() && !bairro.isEmpty() && !uf.isEmpty()) {
            isFormComplete = true;
        } else {
            isFormComplete = false;
        }
    }

    private void avancarParaProximaTela() {
        // Verificar se todos os campos obrigatórios estão preenchidos antes de avançar
        verificarCamposPreenchidos();

        // Verificar se o valor do frete foi calculado e exibido
        String resultadoFreteText = resultadoFrete.getText().toString().trim();

        if (isFormComplete && !resultadoFreteText.isEmpty()) {
            // Limpar o carrinho
            Carrinho.getInstance().limparCarrinho();  // Limpa o carrinho após a compra

            // Navegar para a próxima tela (tela_home, por exemplo)
            Intent in = new Intent(tela_calcular_frete.this, tela_home.class);
            in.putExtra("compraFinalizada", true);  // Passa o sinalizador para a próxima tela
            startActivity(in);
        } else {
            Toast.makeText(this, "Por favor, calcule o frete antes de avançar", Toast.LENGTH_SHORT).show();
        }
    }

    // Método para calcular o frete
    private void calcularFrete() {
        // Verificar se todos os campos obrigatórios estão preenchidos
        verificarCamposPreenchidos();

        String cepDestinoText = cepDestino.getText().toString().trim();

        if (cepDestinoText.isEmpty() || !isFormComplete) {
            Toast.makeText(this, "Por favor, insira todos os campos", Toast.LENGTH_SHORT).show();
            return; // Retorna se o campo não estiver preenchido
        }

        // Calcular o valor do frete (pesoProduto * 1.5)
        String pesoProdutoText = pesoProdutoEditText.getText().toString().trim();
        float pesoProduto = pesoProdutoText.isEmpty() ? 0.0f : Float.parseFloat(pesoProdutoText);

        float valorFrete = pesoProduto * 1.5f;

        // Exibe o resultado do frete
        resultadoFrete.setText(String.format("Valor do frete: R$ %.2f", valorFrete));
    }

    // Método corrigido para o botão Home (voltar)
    public void home(View view) {
        // Navegar para a tela de finalizar compra
        Intent intent = new Intent(tela_calcular_frete.this, tela_menu_geral.class);
        startActivity(intent);
    }
}