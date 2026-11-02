package br.com.zg.acelera.model

import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeParseException

class ServicoCadastro {
    private static final Map<String, Integer> LIMITES_COLUNAS = [
            cpf: 14, cnpj: 18, nome: 255, email: 255, pais: 100, estado: 100, cep: 20
    ]

    private final RepositorioCadastro repositorio

    ServicoCadastro(RepositorioCadastro repositorio) {
        this.repositorio = repositorio
    }

    Map<String, String> cadastrarCandidato(Map<String, Object> dados) {
        String cpf = obterTextoObrigatorio(dados, 'cpf')
        LocalDate dataNascimento
        try {
            dataNascimento = LocalDate.parse(obterTextoObrigatorio(dados, 'dataNascimento'))
        } catch (DateTimeParseException ignored) {
            throw new CadastroInvalidoException(
                    'dataNascimento deve estar no formato ISO (AAAA-MM-DD).'
            )
        }
        if (dataNascimento.isAfter(LocalDate.now())) {
            throw new CadastroInvalidoException('dataNascimento nao pode estar no futuro.')
        }

        repositorio.inserirCandidato([
                cpf      : cpf,
                nome     : obterTextoObrigatorio(dados, 'nome'),
                email    : obterEmailObrigatorio(dados),
                pais     : obterTextoObrigatorio(dados, 'pais'),
                estado   : obterTextoObrigatorio(dados, 'estado'),
                cep      : obterTextoObrigatorio(dados, 'cep'),
                descricao: obterTextoObrigatorio(dados, 'descricao'),
                idade    : Period.between(dataNascimento, LocalDate.now()).years
        ])
        [tipo: 'candidato', identificador: cpf]
    }

    Map<String, String> cadastrarEmpresa(Map<String, Object> dados) {
        String cnpj = obterTextoObrigatorio(dados, 'cnpj')
        repositorio.inserirEmpresa([
                cnpj     : cnpj,
                nome     : obterTextoObrigatorio(dados, 'nome'),
                email    : obterEmailObrigatorio(dados),
                pais     : obterTextoObrigatorio(dados, 'pais'),
                estado   : obterTextoObrigatorio(dados, 'estado'),
                cep      : obterTextoObrigatorio(dados, 'cep'),
                descricao: obterTextoObrigatorio(dados, 'descricao')
        ])
        [tipo: 'empresa', identificador: cnpj]
    }

    private static String obterEmailObrigatorio(Map<String, Object> dados) {
        String email = obterTextoObrigatorio(dados, 'email')
        if (!(email ==~ /^[^\s@]+@[^\s@]+\.[^\s@]+$/)) {
            throw new CadastroInvalidoException('email invalido.')
        }
        email
    }

    private static String obterTextoObrigatorio(Map<String, Object> dados, String campo) {
        Object valor = dados[campo]
        if (!(valor instanceof String) || !valor.trim()) {
            throw new CadastroInvalidoException("${campo} e obrigatorio e deve ser texto.")
        }
        String texto = valor.trim()
        Integer limite = LIMITES_COLUNAS[campo]
        if (limite != null && texto.length() > limite) {
            throw new CadastroInvalidoException("${campo} excede o limite de ${limite} caracteres.")
        }
        texto
    }
}
