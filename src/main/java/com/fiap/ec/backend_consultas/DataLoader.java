package com.fiap.ec.backend_consultas;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.fiap.ec.backend_consultas.model.Consulta;
import com.fiap.ec.backend_consultas.model.Especialidade;
import com.fiap.ec.backend_consultas.model.Medico;
import com.fiap.ec.backend_consultas.model.Paciente;
import com.fiap.ec.backend_consultas.repository.ConsultaRepository;
import com.fiap.ec.backend_consultas.repository.EspecialidadeRepository;
import com.fiap.ec.backend_consultas.repository.MedicoRepository;
import com.fiap.ec.backend_consultas.repository.PacienteRepository;

/**
 * DataLoader — executado automaticamente ao iniciar o backend.
 *
 * Semeia todos os dados de exemplo caso as tabelas estejam vazias.
 * Isso garante que o app funcione tanto localmente quanto na nuvem
 * (onde o banco H2 começa do zero a cada reinicialização do serviço).
 *
 * Ordem de inserção:
 *   1. Especialidades  (não depende de ninguém)
 *   2. Médicos         (dependem de especialidades)
 *   3. Pacientes       (não dependem de ninguém)
 *   4. Consultas       (dependem de médicos e pacientes)
 */
@Component
public class DataLoader implements CommandLineRunner {

    private final EspecialidadeRepository especialidadeRepository;
    private final MedicoRepository medicoRepository;
    private final PacienteRepository pacienteRepository;
    private final ConsultaRepository consultaRepository;

    public DataLoader(EspecialidadeRepository especialidadeRepository,
                      MedicoRepository medicoRepository,
                      PacienteRepository pacienteRepository,
                      ConsultaRepository consultaRepository) {
        this.especialidadeRepository = especialidadeRepository;
        this.medicoRepository = medicoRepository;
        this.pacienteRepository = pacienteRepository;
        this.consultaRepository = consultaRepository;
    }

    @Override
    public void run(String... args) throws Exception {

        // ── 1. ESPECIALIDADES ───────────────────────────────────────────────
        if (especialidadeRepository.count() == 0) {
            especialidadeRepository.saveAll(List.of(
                new Especialidade("Cardiologia",   "Especialidade do coração"),
                new Especialidade("Dermatologia",  "Tratamento de doenças da pele"),
                new Especialidade("Ortopedia",     "Sistema músculo-esquelético"),
                new Especialidade("Pediatria",     "Saúde de crianças e adolescentes"),
                new Especialidade("Neurologia",    "Sistema nervoso central e periférico"),
                new Especialidade("Ginecologia",   "Saúde da mulher"),
                new Especialidade("Oftalmologia",  "Saúde dos olhos")
            ));
            System.out.println("DataLoader: 7 especialidades criadas.");
        }

        // ── 2. MÉDICOS ──────────────────────────────────────────────────────
        if (medicoRepository.count() == 0) {
            List<Especialidade> especialidades = especialidadeRepository.findAll();
            Especialidade cardio  = especialidades.get(0); // Cardiologia
            Especialidade derma   = especialidades.get(1); // Dermatologia
            Especialidade ortop   = especialidades.get(2); // Ortopedia
            Especialidade pedia   = especialidades.get(3); // Pediatria
            Especialidade neuro   = especialidades.get(4); // Neurologia

            List<Medico> medicos = medicoRepository.saveAll(List.of(
                medico("Dr. Roberto Silva",     "789456", cardio,  750.00),
                medico("Dra. Ana Ferreira",     "123789", derma,   480.00),
                medico("Dr. Carlos Mendes",     "456123", ortop,   550.00),
                medico("Dra. Patricia Lima",    "321654", pedia,   420.00),
                medico("Dr. Fernando Souza",    "654321", neuro,   680.00)
            ));
            System.out.println("DataLoader: " + medicos.size() + " médicos criados.");
        }

        // ── 3. PACIENTES ────────────────────────────────────────────────────
        if (pacienteRepository.count() == 0) {
            pacienteRepository.saveAll(List.of(
                paciente("Maria Silva",       "12345678901", "maria@email.com",   "11999991111", "1990-03-15"),
                paciente("João Santos",       "98765432100", "joao@email.com",    "11988882222", "1985-07-22"),
                paciente("Ana Costa",         "11122233344", "ana@email.com",     null,          "1995-11-08"),
                paciente("Pedro Oliveira",    "55544433322", "pedro@email.com",   "11977773333", "1978-01-30"),
                paciente("Lucia Fernandes",   "66677788899", "lucia@email.com",   "11966664444", "2001-05-17")
            ));
            System.out.println("DataLoader: 5 pacientes criados.");
        }

        // ── 4. CONSULTAS ────────────────────────────────────────────────────
        if (consultaRepository.count() == 0) {
            List<Medico>   medicos   = medicoRepository.findAll();
            List<Paciente> pacientes = pacienteRepository.findAll();

            if (medicos.isEmpty() || pacientes.isEmpty()) {
                System.out.println("DataLoader: sem médicos ou pacientes — consultas não criadas.");
                return;
            }

            Medico   m1 = medicos.get(0);   // Dr. Roberto Silva
            Medico   m2 = medicos.get(1);   // Dra. Ana Ferreira
            Medico   m3 = medicos.get(2);   // Dr. Carlos Mendes
            Paciente p1 = pacientes.get(0); // Maria Silva
            Paciente p2 = pacientes.get(1); // João Santos
            Paciente p3 = pacientes.get(2); // Ana Costa

            consultaRepository.saveAll(List.of(
                new Consulta(m1, p1, LocalDateTime.of(2026, 10, 5,  9,  0), "agendada",   750.0,  "Consulta de rotina"),
                new Consulta(m2, p2, LocalDateTime.of(2026, 10, 6,  14, 30),"confirmada", 480.0,  "Retorno pós-exame"),
                new Consulta(m3, p3, LocalDateTime.of(2026, 10, 7,  10, 0), "agendada",   550.0,  null),
                new Consulta(m1, p2, LocalDateTime.of(2026,  9, 20, 11, 0), "realizada",  750.0,  "Exame de sangue em dia"),
                new Consulta(m2, p3, LocalDateTime.of(2026,  9, 18, 16, 0), "cancelada",  480.0,  "Paciente desmarcou"),
                new Consulta(m3, p1, LocalDateTime.of(2026, 10, 12, 8,  30),"agendada",   550.0,  "Primeira consulta")
            ));
            System.out.println("DataLoader: 6 consultas de exemplo criadas.");
        }

        System.out.println("DataLoader: banco de dados pronto.");
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Medico medico(String nome, String crm, Especialidade esp, double valor) {
        Medico m = new Medico();
        m.setNome(nome);
        m.setCrm(crm);
        m.setEspecialidade(esp);
        m.setAtivo(true);
        m.setValorConsulta(valor);
        return m;
    }

    private Paciente paciente(String nome, String cpf, String email,
                              String telefone, String dataNasc) {
        Paciente p = new Paciente();
        p.setNome(nome);
        p.setCpf(cpf);
        p.setEmail(email);
        p.setTelefone(telefone);
        p.setDataNascimento(LocalDate.parse(dataNasc));
        p.setAtivo(true);
        return p;
    }
}
