package com.bootcamp.estudiante;

import com.bootcamp.cuenta.CuentaBancaria;
import com.bootcamp.cuenta.CuentaBancariaRepository;
import com.bootcamp.libro.Libro;
import com.bootcamp.libro.LibroRepository;
import com.bootcamp.materia.Materia;
import com.bootcamp.materia.MateriaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

@Transactional
@Service
public class EstudianteService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EstudianteService.class);
    private final EstudianteRepository estudianteRepository;
    private final LibroRepository libroRepository;
    private final MateriaRepository materiaRepository;
    private final CuentaBancariaRepository cuentaBancariaRepository;

    @Autowired
    public EstudianteService(EstudianteRepository estudianteRepository,
                             LibroRepository libroRepository,
                             MateriaRepository materiaRepository,
                             CuentaBancariaRepository cuentaBancariaRepository) {
        this.estudianteRepository = estudianteRepository;
        this.libroRepository = libroRepository;
        this.materiaRepository = materiaRepository;
        this.cuentaBancariaRepository = cuentaBancariaRepository;
    }

    @Transactional(readOnly = true)
    public List<Estudiante> getAllEstudiantes() {
        List<Estudiante> estudiantes = estudianteRepository.findAll();

        return estudiantes;
    }

    @Transactional(readOnly = true)
    public Page<Estudiante> findAllEstudiantes(Pageable pageable) {
        return estudianteRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<Estudiante> getEstudiantesByPrimerNombreOrPrimerApellido(String primerNombre, String primerApellido) {
        List<Estudiante> estudiantes = estudianteRepository.findEstudianteByPrimerNombreOrPrimerApellido(primerNombre, primerApellido);

        return estudiantes;
    }

    public Long createEstudiante(Estudiante e) {
        LOGGER.info("creando estudiante {} ", e);
        // check si el email es valido
        if(!checkValidezEmail(e.getEmail())) {
            LOGGER.warn("Email {} no es valido", e.getEmail());
            throw new IllegalArgumentException("Email " + e.getEmail() + " no es valido");
        }

        // check si el email ya existe
        boolean emailExiste = estudianteRepository.existsByEmail(e.getEmail());
        if (emailExiste) {
            LOGGER.warn("Email {} ya esta registrado", e.getEmail());
            throw new IllegalArgumentException("Email " + e.getEmail() + " ya esta registrado");
        }

        Long id = estudianteRepository.save(e).getId();
        LOGGER.info("Estudiante con id {} fue guardado exitosamente", id);
        return id;
    }

    public void deleteEstudiante(Long estudianteId) {
        // check si id existe, si no botamos exception
        boolean estudianteExiste = estudianteRepository.existsById(estudianteId);

        if (!estudianteExiste) {
            throw new NoSuchElementException("Estudiante con id " + estudianteId + " no existe");
        }

        estudianteRepository.deleteById(estudianteId);
    }

    public Estudiante updateEstudiante(Long id, Estudiante estudianteAActualizar) {
        // check si estudiante con ese id existe, si no botamos un Error
        Estudiante estudianteExistente = estudianteRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Estudiante con ese id no existe, id: " + id));

        // Actualizar estudiante
        Nombre nombre = new Nombre();
        nombre.setPrimerNombre(estudianteAActualizar.getNombre().getPrimerNombre());
        nombre.setSegundoNombre(estudianteAActualizar.getNombre().getSegundoNombre());
        nombre.setPrimerApellido(estudianteAActualizar.getNombre().getPrimerApellido());
        nombre.setSegundoApellido(estudianteAActualizar.getNombre().getSegundoApellido());
        estudianteExistente.setNombre(nombre);
        estudianteExistente.setFechaNacimiento(estudianteAActualizar.getFechaNacimiento());

        // check si el email es valido
        if(!checkValidezEmail(estudianteAActualizar.getEmail())) {
            throw new IllegalArgumentException("Email " + estudianteAActualizar.getEmail() + " no es valido");
        }

        // check si el email que se quiere actualizar ya existe
        boolean emailExiste = estudianteRepository.existsByEmailAndIdIsNot(estudianteAActualizar.getEmail(), id);
        if (emailExiste) {
            throw new IllegalArgumentException("Email " + estudianteAActualizar.getEmail() + " ya esta registrado");
        }

       // Actualizar email
        estudianteExistente.setEmail(estudianteAActualizar.getEmail());

        return estudianteExistente;
    }

    @Transactional(readOnly = true)
    public Estudiante getEstudiante(Long id) {
        return estudianteRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Estudiante con id " + id + " no existe"));
    }

    private boolean checkValidezEmail(String email) {
        return Pattern.compile(
                "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",
                Pattern.CASE_INSENSITIVE
        ).asPredicate().test(email);
    }

    public Estudiante agregarLibroAEstudiante(Long estudianteId, Long libroId) {
        // check si estudiante con ese id existe, si no botamos un Error
        Estudiante estudianteExistente = estudianteRepository.findById(estudianteId)
                .orElseThrow(() -> new NoSuchElementException("Estudiante con ese id no existe, id: " + estudianteId));

        // check si libro con ese id existe, si no botamos un Error
        Libro libroExistente = libroRepository.findById(libroId)
                .orElseThrow(() -> new NoSuchElementException("Libro con ese id no existe, id: " + libroId));

        libroExistente.setEstudiante(estudianteExistente);
//        estudianteExistente.addLibro(libroExistente);
        return estudianteExistente;
    }

    public Estudiante agregarMateriaAEstudiante(Long estudianteId, Long materiaId) {
        // check si estudiante con ese id existe, si no botamos un Error
        Estudiante estudianteExistente = estudianteRepository.findById(estudianteId)
                .orElseThrow(() -> new NoSuchElementException("Estudiante con ese id no existe, id: " + estudianteId));

        // check si materia con ese id existe, si no botamos un Error
        Materia materiaExistente = materiaRepository.findById(materiaId)
                .orElseThrow(() -> new NoSuchElementException("Materia con ese id no existe, id: " + materiaId));

        estudianteExistente.addMateria(materiaExistente);
        return estudianteExistente;
    }

    public Estudiante agregarCuentaAEstudiante(Long estudianteId, Long cuentaId) {

        // check si estudiante con ese id existe, si no botamos un Error
        Estudiante estudianteExistente = estudianteRepository.findById(estudianteId)
                .orElseThrow(() -> new NoSuchElementException("Estudiante con ese id no existe, id: " + estudianteId));

        // check si cuenta con ese id existe, si no botamos un Error
        CuentaBancaria cuentaExistente = cuentaBancariaRepository.findById(cuentaId)
                .orElseThrow(() -> new NoSuchElementException("Cuenta con ese id no existe, id: " + cuentaId));

        estudianteExistente.setCuenta(cuentaExistente);
        return estudianteExistente;
    }
}
