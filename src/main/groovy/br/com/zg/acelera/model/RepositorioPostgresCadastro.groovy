package br.com.zg.acelera.model

import java.sql.DriverManager

class RepositorioPostgresCadastro implements RepositorioCadastro {
    private final String urlBanco
    private final String usuarioBanco
    private final String senhaBanco

    RepositorioPostgresCadastro(String urlBanco, String usuarioBanco, String senhaBanco) {
        this.urlBanco = urlBanco
        this.usuarioBanco = usuarioBanco
        this.senhaBanco = senhaBanco
    }

    @Override
    void inserirCandidato(Map<String, Object> candidato) {
        String sql = '''
            INSERT INTO candidato (cpf, nome, email, pais, estado, cep, descricao, idade)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        '''
        comConexao { conexao ->
            conexao.prepareStatement(sql).withCloseable { instrucao ->
                instrucao.setString(1, candidato.cpf as String)
                instrucao.setString(2, candidato.nome as String)
                instrucao.setString(3, candidato.email as String)
                instrucao.setString(4, candidato.pais as String)
                instrucao.setString(5, candidato.estado as String)
                instrucao.setString(6, candidato.cep as String)
                instrucao.setString(7, candidato.descricao as String)
                instrucao.setInt(8, candidato.idade as int)
                instrucao.executeUpdate()
            }
        }
    }

    @Override
    void inserirEmpresa(Map<String, Object> empresa) {
        String sql = '''
            INSERT INTO empresa (cnpj, nome, email, pais, estado, cep, descricao)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        '''
        comConexao { conexao ->
            conexao.prepareStatement(sql).withCloseable { instrucao ->
                instrucao.setString(1, empresa.cnpj as String)
                instrucao.setString(2, empresa.nome as String)
                instrucao.setString(3, empresa.email as String)
                instrucao.setString(4, empresa.pais as String)
                instrucao.setString(5, empresa.estado as String)
                instrucao.setString(6, empresa.cep as String)
                instrucao.setString(7, empresa.descricao as String)
                instrucao.executeUpdate()
            }
        }
    }

    private <T> T comConexao(Closure<T> operacao) {
        DriverManager.getConnection(urlBanco, usuarioBanco, senhaBanco).withCloseable {
            conexao -> operacao.call(conexao)
        }
    }
}
