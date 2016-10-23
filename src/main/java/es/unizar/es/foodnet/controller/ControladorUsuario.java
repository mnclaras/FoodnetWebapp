package es.unizar.es.foodnet.controller;

import es.unizar.es.foodnet.model.entity.Usuario;
import es.unizar.es.foodnet.model.repository.RepositorioUsuario;
import es.unizar.es.foodnet.model.service.Password;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Controller
public class ControladorUsuario {

    @Autowired
    private RepositorioUsuario repoUsuario;

    /**
     * Obtiene un usuario del formulario html e intenta registrarlo
     * @param user usuario a registrar
     * @return redireccion a index
     */
    @RequestMapping(value="/registrarse", method = RequestMethod.POST)
    public String registrarUsuario(Usuario user){
        System.out.println("Detectada peticion para registrar al usuario " + user.getEmail());
        Password pw = new Password();

        //ciframos la password del usuario
        try {
            user.setPassword(pw.generatePassword(user.getPassword()));
            repoUsuario.save(user);
        } catch (Exception e) {
            System.err.println("Error al generar password cifrada del usuario " + user.getEmail());
        }

        return "redirect:/";
    }

    /**
     * Autentica un usuario si existe e ingresa la password adecuada
     * @param email email con el que se quiere hacer login
     * @param password password con el que se quiere hacer login
     * @param request objeto request del usuario
     * @return redireccion a catalogo si ha ido bien, redireccion a index si ha habido algun fallo
     */
    @RequestMapping(value="/login", method = RequestMethod.POST)
    public String autenticarUsuario(@RequestParam("email") String email,
                                    @RequestParam("password") String password,
                                    HttpServletRequest request){
        System.out.println("Detectada peticion para que el usuario " + email + " haga login");

        Password pw = new Password();

        Usuario user = repoUsuario.findByEmail(email);

        if(user != null){
            try {
                if(pw.isPasswordValid(password,user.getPassword())){
                    System.out.println("Password valida, redirigiendo a catalogo");
                    request.getSession().setAttribute("user", user);
                    return "redirect:/catalogo";
                } else{
                    System.out.println("Password no valida");
                    return "redirect:/";
                }
            } catch (Exception e) {
                System.err.println("Error al comprobar la password del usuario " + user.getEmail());
                return "redirect:/";
            }
        } else{
            System.err.println("Error al obtener el usuario cuyo email es " + email);
            return "redirect:/";
        }

    }

    /**
     * Comprueba si un email existe ya en la bbdd
     * @param email email a comprobar
     * @param response 'noencontrado' si no existe, 'encontrado' en caso contrario
     */
    @RequestMapping(value="/comprobarEmail", method= RequestMethod.POST)
    public void comprobarEmailRegistro(@RequestParam("email") String email,
                                   HttpServletResponse response){
        System.out.println("Me ha llegado peticion para comprobar si " + email +  " existe");

        Usuario user = repoUsuario.findByEmail(email);
        PrintWriter out;
        try {
            out = response.getWriter();

            if(user != null){
                out.println("encontrado");
                System.out.println(email + " existe.");
            } else{
                out.println("noencontrado");
                System.out.println(email + " no existe.");
            }
        } catch (IOException e) {
            System.err.println("Error al obtener la salida del response del usuario");
        }
    }
}