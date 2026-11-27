package br.unicamp.ic.mc322.model;

import br.unicamp.ic.mc322.atividades.Atividade;
import br.unicamp.ic.mc322.exceptions.DadoInvalidoException;
import br.unicamp.ic.mc322.exceptions.PrazoInvalidoException;
import br.unicamp.ic.mc322.persistencia.Csv;
import br.unicamp.ic.mc322.persistencia.PersistivelCSV;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/** Um semestre do curso (F1 a F5). Compõe as disciplinas cursadas nele. */
public class Semestre implements PersistivelCSV {
    private String id;
    private int ano;
    private int numeroSemestre;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private LocalDate dataLimiteTrancamento;
    private final List<Disciplina> disciplinas = new ArrayList<>();

    public Semestre(int ano, int num, LocalDate ini, LocalDate fim, LocalDate limTranc) throws PrazoInvalidoException {
        this.id = UUID.randomUUID().toString();
        editar(ano, num, ini, fim, limTranc);
    }

    /** F2: editar os dados do semestre. */
    public void editar(int ano, int num, LocalDate ini, LocalDate fim, LocalDate limTranc) throws PrazoInvalidoException {
        if (ano < 2000 || ano > 2100) {
            throw new PrazoInvalidoException("Ano do semestre inválido: " + ano + ".");
        }
        if (num < 1 || num > 20) {
            throw new PrazoInvalidoException("O número do semestre no curso deve ficar entre 1 e 20.");
        }
        validarDatas(ini, fim, limTranc);
        this.ano = ano;
        this.numeroSemestre = num;
        this.dataInicio = ini;
        this.dataFim = fim;
        this.dataLimiteTrancamento = limTranc;
    }

    public void validarDatas(LocalDate ini, LocalDate fim, LocalDate lim) throws PrazoInvalidoException {
        if (ini == null || fim == null) {
            throw new PrazoInvalidoException("Informe as datas de início e de fim do semestre.");
        }
        if (!fim.isAfter(ini)) {
            throw new PrazoInvalidoException("O semestre precisa terminar depois de começar.");
        }
        if (lim != null && (lim.isBefore(ini) || lim.isAfter(fim))) {
            throw new PrazoInvalidoException("O limite de trancamento precisa estar dentro do semestre.");
        }
    }

    /** F6: cadastra uma disciplina nova neste semestre. */
    public Disciplina cadastrarDisciplina(String cod, String nome, String prof, int cred) throws DadoInvalidoException {
        Disciplina d = new Disciplina(cod, nome, prof, cred);
        adicionarDisciplina(d);
        return d;
    }

    /** Liga uma disciplina já criada (usado também ao carregar os arquivos). */
    public void adicionarDisciplina(Disciplina d) throws DadoInvalidoException {
        if (buscarDisciplina(d.getCodigo()) != null) {
            throw new DadoInvalidoException("Já existe a disciplina " + d.getCodigo() + " neste semestre.");
        }
        d.setSemestre(this);
        disciplinas.add(d);
    }

    public void removerDisciplina(Disciplina d) {
        if (disciplinas.remove(d)) {
            d.setSemestre(null);
        }
    }

    public Disciplina buscarDisciplina(String codigo) {
        for (Disciplina d : disciplinas) {
            if (d.getCodigo().equalsIgnoreCase(codigo)) {
                return d;
            }
        }
        return null;
    }

    /** Verdadeiro se o horário novo colide com alguma aula de disciplina não trancada (ignorando "ignorar"). */
    public boolean verificarConflitoHorario(HorarioAula novo, HorarioAula ignorar) {
        for (Disciplina d : disciplinas) {
            if (d.isTrancada()) {
                continue;
            }
            for (HorarioAula h : d.getHorarios()) {
                if (h != ignorar && h != novo && h.conflitaCom(novo)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** F30: todas as aulas do semestre em ordem de dia e hora. */
    public List<HorarioAula> obterGradeSemanal() {
        List<HorarioAula> grade = new ArrayList<>();
        for (Disciplina d : disciplinas) {
            if (!d.isTrancada()) {
                grade.addAll(d.getHorarios());
            }
        }
        Collections.sort(grade);
        return grade;
    }

    public List<HorarioAula> obterAulasDoDia(DiaSemana dia) {
        List<HorarioAula> aulas = new ArrayList<>();
        for (HorarioAula h : obterGradeSemanal()) {
            if (h.getDiaSemana() == dia) {
                aulas.add(h);
            }
        }
        return aulas;
    }

    /**
     * F31: a próxima aula a partir de um dia e hora. Procura no mesmo dia (aulas que ainda
     * não começaram) e depois nos dias seguintes da semana. Devolve null se não houver aulas.
     */
    public HorarioAula obterProximaAula(DiaSemana dia, LocalTime hora) {
        List<HorarioAula> grade = obterGradeSemanal();
        if (grade.isEmpty()) {
            return null;
        }
        int inicio = dia == null ? 0 : dia.ordinal();
        DiaSemana[] dias = DiaSemana.values();
        for (int i = 0; i <= dias.length; i++) {
            DiaSemana d = dias[(inicio + i) % dias.length];
            for (HorarioAula h : grade) {
                if (h.getDiaSemana() != d) {
                    continue;
                }
                boolean mesmoDia = i == 0 && dia != null;
                if (!mesmoDia || h.getHoraInicio().isAfter(hora)) {
                    return h;
                }
            }
        }
        return grade.get(0);
    }

    public boolean contem(LocalDate data) {
        return data != null && !data.isBefore(dataInicio) && !data.isAfter(dataFim);
    }

    /** Progresso do semestre (F23): fração das atividades concluídas, de 0 a 1. */
    public double calcularProgresso() {
        int total = 0;
        int concluidas = 0;
        for (Disciplina d : disciplinas) {
            if (d.isTrancada()) {
                continue;
            }
            total += d.getAtividades().size();
            concluidas += (int) d.getAtividades().stream()
                    .filter(Atividade::estaEncerrada).count();
        }
        return total == 0 ? 0 : (double) concluidas / total;
    }

    public String getRotulo() {
        return ano + " – " + numeroSemestre + "º semestre";
    }

    public String getId() {
        return id;
    }

    public void restaurarId(String id) {
        this.id = id;
    }

    public int getAno() {
        return ano;
    }

    public int getNumeroSemestre() {
        return numeroSemestre;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public LocalDate getDataLimiteTrancamento() {
        return dataLimiteTrancamento;
    }

    public List<Disciplina> getDisciplinas() {
        return Collections.unmodifiableList(disciplinas);
    }

    @Override
    public String toCSV() {
        return Csv.juntar(id, ano, numeroSemestre, dataInicio, dataFim, dataLimiteTrancamento);
    }

    @Override
    public String toString() {
        return getRotulo();
    }
}
