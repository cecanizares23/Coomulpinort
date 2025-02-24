/*
 * ContextDataResourceNames.java
 *
 * Proyecto: Coomulpinort Puntos
 * Cliente:  Coomulpinort
 * Copyright 2020 by Ing. Carlos Cañizares
 * All rights reserved
 */

package co.IngCarlos.Coomulpinort.mvc.mediador;

import co.IngCarlos.Coomulpinort.common.connection.ContextDataResourceNames;
import co.IngCarlos.Coomulpinort.common.connection.DataBaseConnection;
import co.IngCarlos.Coomulpinort.common.util.Constantes;
import co.IngCarlos.Coomulpinort.common.util.EnvioEmail;
import co.IngCarlos.Coomulpinort.common.util.Formato;
import co.IngCarlos.Coomulpinort.common.util.Generales;
import co.IngCarlos.Coomulpinort.common.util.LoggerMessage;
import co.IngCarlos.Coomulpinort.mvc.dao.DatosUsuarioDAO;
import co.IngCarlos.Coomulpinort.mvc.dao.TipoDocumentoDAO;
import co.IngCarlos.Coomulpinort.mvc.dao.TipoUsuarioDAO;
import co.IngCarlos.Coomulpinort.mvc.dao.UsuarioDAO;
import co.IngCarlos.Coomulpinort.mvc.dao.UsuarioSeguridadDAO;
import co.IngCarlos.Coomulpinort.mvc.dto.BodyMensajeDTO;
import co.IngCarlos.Coomulpinort.mvc.dto.DatosUsuarioDTO;
import co.IngCarlos.Coomulpinort.mvc.dto.RegistroDTO;
import co.IngCarlos.Coomulpinort.mvc.dto.RespuestaDTO;
import co.IngCarlos.Coomulpinort.mvc.dto.TipoDocumentoDTO;
import co.IngCarlos.Coomulpinort.mvc.dto.TipoUsuarioDTO;
import co.IngCarlos.Coomulpinort.mvc.dto.UsuarioDTO;
import co.IngCarlos.Coomulpinort.mvc.dto.UsuarioSeguridadDTO;
import co.IngCarlos.Coomulpinort.responsemensaje.dto.ResponseMensajeDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.crypto.provider.ARCFOURCipher;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import static java.lang.System.out;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.InitialContext;
import javax.net.ssl.HttpsURLConnection;
import javax.servlet.http.HttpSession;
import org.directwebremoting.WebContextFactory;
import org.json.JSONObject;
import util.ServicioEmail;

/**
 *
 * @author carlos
 */
public class MediadorAppGastos {

    /**
     *
     */
    private final static MediadorAppGastos instancia = null;
    private final LoggerMessage logMsg;

    /**
     *
     */
    public MediadorAppGastos() {
        logMsg = LoggerMessage.getInstancia();
    }

    /**
     *
     * @return
     */
    public static synchronized MediadorAppGastos getInstancia() {
        return instancia == null ? new MediadorAppGastos() : instancia;
    }

    /**
     * -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
     * LOS METODOS APARTIR DE AQUI NO HAN SIDO VALIDADOS
     * -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
     */
    /**
     *
     * @param datosUsuario1
     * @param datosUsuarioSeguridad
     * @return
     */
    public RegistroDTO registrarUsuario(UsuarioDTO datosUsuario1, UsuarioSeguridadDTO datosUsuarioSeguridad) {

        HttpSession session = WebContextFactory.get().getSession();
        DatosUsuarioDTO datosUsuario = (DatosUsuarioDTO) session.getAttribute("datosUsuario");
        DataBaseConnection dbcon = null;
        Connection conexion = null;
        boolean registroUsuario = false;
        boolean registroUsuarioSeguridad = false;
        String codigoEjecutivo = null;
        String rangoMinimo = null;
        String rangoMaximo = null;
        RegistroDTO registroExitoso = null;

        try {
            dbcon = DataBaseConnection.getInstance();
            conexion = dbcon.getConnection(ContextDataResourceNames.MYSQL_COOMULPINORT_JDBC);
            conexion.setAutoCommit(false);
            registroExitoso = new RegistroDTO();
            String fecha = Formato.formatoFecha(datosUsuario1.getFechaNacimiento());
            datosUsuario1.setFechaNacimiento(fecha);

            registroUsuario = new UsuarioDAO().registrarUsuario(conexion, datosUsuario1, datosUsuario.getUsuario());

            registroUsuarioSeguridad = new UsuarioSeguridadDAO().registrarUsuarioSeguridad(conexion, datosUsuario1, datosUsuarioSeguridad, datosUsuario.getUsuario());

            if (registroUsuario != false && registroUsuarioSeguridad != false) {

                String[] to = {datosUsuario1.getCorreo()};
                String estado = "CONFIRMACIÓN: SE CREÓ LA CUENTA EN LA PLATAFORMA Gastos QX CSJ";
                String body = "Cordial Saludo.\n\n\n"
                        + "En el presente correo se confirma el registro en la plataforma Medipin, con los siguientes datos: \n\n"
                        + "Nombre: " + datosUsuario1.getNombre() + "\n"
                        + "NIT: " + datosUsuario1.getDocumento() + "\n"
                        + "Correo: " + datosUsuario1.getCorreo() + "\n"
                        + "Usuario: " + datosUsuarioSeguridad.getUsuario() + "\n"
                        + "Clave Medipin: " + datosUsuarioSeguridad.getClave() + "\n\n"
                        + "Para ingresar a la plataforma GastosQX ingrese en el siguiente enlace: www.algo.com \n"
                        + "Este correo se generÃ³ automÃ¡ticamente por lo tanto no se debe responder al mismo.";

                System.out.println("datosUsuario1 " + datosUsuario1.toStringJson());
                System.out.println("TO " + to + "estado " + estado + "body " + body);

                //ServicioEmail s = new ServicioEmail(Constantes.CORREO, Constantes.CLAVE_CORREO);
                boolean enviar = EnvioEmail.sendFromGMail(Constantes.CORREO, Constantes.CLAVE_CORREO, to, estado, body);

                //s.enviarEmail(datosUsuario1.getCorreo(), estado, body);
                if (enviar == true) {
                    conexion.commit();
                    registroExitoso.setCondicion(true);
                    registroExitoso.setMensaje("Registro Exitoso");
                    logMsg.loggerMessageDebug(" |  Se registrÃ³ la revisiÃ³n exitosamente");
                } else {
                    conexion.rollback();
                    registroExitoso = null;
                    throw new Exception("Error ::: al enviar el email");
                }

            } else {
                conexion.rollback();
                registroExitoso = null;
            }
        } catch (Exception e) {
            LoggerMessage.getInstancia().loggerMessageException(e);
        } finally {
            try {
                if (conexion != null && !conexion.isClosed()) {
                    conexion.close();
                    conexion = null;
                }
            } catch (Exception e) {
                LoggerMessage.getInstancia().loggerMessageException(e);
            }
        }
        return registroExitoso;
    }

    /**
     *
     * @param datosUsuario
     * @return
     */
    public boolean validarUsuario(UsuarioSeguridadDTO datosUsuario) {

        HttpSession session = WebContextFactory.get().getSession();
        DataBaseConnection dbcon = null;
        Connection conexion = null;

        boolean validarUsuario = false;

        try {

            dbcon = DataBaseConnection.getInstance();
            conexion = dbcon.getConnection(ContextDataResourceNames.MYSQL_COOMULPINORT_JDBC);
            conexion.setAutoCommit(false);

            validarUsuario = new UsuarioSeguridadDAO().validarUsuarioSeguridad(conexion, datosUsuario);

            if (validarUsuario != false) {
                conexion.commit();
                validarUsuario = true;
            } else {
                conexion.rollback();
            }
        } catch (Exception e) {
            LoggerMessage.getInstancia().loggerMessageException(e);
        } finally {
            try {
                if (conexion != null && !conexion.isClosed()) {
                    conexion.close();
                    conexion = null;
                }
            } catch (Exception e) {
                LoggerMessage.getInstancia().loggerMessageException(e);
            }
        }
        return validarUsuario;
    }

    /**
     *
     * @return
     */
    public ArrayList<UsuarioDTO> listarUsuarioSeguridad() {
        DataBaseConnection dbcon = null;
        Connection conexion = null;
        ArrayList<UsuarioDTO> listado = null;
        try {
            dbcon = DataBaseConnection.getInstance();
            conexion = dbcon.getConnection(ContextDataResourceNames.MYSQL_COOMULPINORT_JDBC);
            listado = new UsuarioDAO().listarUsuarioSeguridad(conexion);
            conexion.close();
            conexion = null;
        } catch (Exception e) {
            LoggerMessage.getInstancia().loggerMessageException(e);
        } finally {
            try {
                if (conexion != null && !conexion.isClosed()) {
                    conexion.close();
                    conexion = null;
                }
            } catch (Exception e) {
                LoggerMessage.getInstancia().loggerMessageException(e);
            }
        }
        return listado;
    }

    /**
     *
     * @param idUsuario
     * @return
     */
    public UsuarioDTO consultarUsuario(String idUsuario) {
        DataBaseConnection dbcon = null;
        Connection conexion = null;
        UsuarioDTO datos = null;

        try {

            dbcon = DataBaseConnection.getInstance();
            conexion = dbcon.getConnection(ContextDataResourceNames.MYSQL_COOMULPINORT_JDBC);

            datos = new UsuarioDAO().consultarUsuario(conexion, idUsuario);
            //String fecha = Formato.formatoFechaMostrar(datos.getFechaNacimiento());            
            datos.setFechaNacimiento(Formato.formatoFechaMostrar(datos.getFechaNacimiento()));
            conexion.close();
            conexion = null;
        } catch (Exception e) {
            LoggerMessage.getInstancia().loggerMessageException(e);
        } finally {
            try {
                if (conexion != null && !conexion.isClosed()) {
                    conexion.close();
                    conexion = null;
                }
            } catch (Exception e) {
                LoggerMessage.getInstancia().loggerMessageException(e);
            }
        }

        return datos;
    }

    /**
     *
     * @return
     */
    public ArrayList<UsuarioDTO> listarUsuarios() {
        DataBaseConnection dbcon = null;
        Connection conexion = null;
        ArrayList<UsuarioDTO> listado = null;

        try {

            dbcon = DataBaseConnection.getInstance();
            conexion = dbcon.getConnection(ContextDataResourceNames.MYSQL_COOMULPINORT_JDBC);
            listado = new UsuarioDAO().listarUsuarios(conexion);

            conexion.close();
            conexion = null;
        } catch (Exception e) {
            LoggerMessage.getInstancia().loggerMessageException(e);
        } finally {
            try {
                if (conexion != null && !conexion.isClosed()) {
                    conexion.close();
                    conexion = null;
                }
            } catch (Exception e) {
                LoggerMessage.getInstancia().loggerMessageException(e);
            }
        }
        return listado;
    }

    /**
     *
     * @param id
     * @return
     */
    public boolean eliminarUsuario(String id) {

        DataBaseConnection dbcon = null;
        Connection conexion = null;
        boolean deleteExitoso = false;

        try {

            dbcon = DataBaseConnection.getInstance();
            conexion = dbcon.getConnection(ContextDataResourceNames.MYSQL_COOMULPINORT_JDBC);
            deleteExitoso = new UsuarioDAO().eliminarUsuario(conexion, id);

            conexion.close();
            conexion = null;

        } catch (Exception e) {
            LoggerMessage.getInstancia().loggerMessageException(e);
            deleteExitoso = false;
        } finally {
            try {
                if (conexion != null && !conexion.isClosed()) {
                    conexion.close();
                    conexion = null;
                }
            } catch (SQLException e) {
                LoggerMessage.getInstancia().loggerMessageException(e);
                deleteExitoso = false;
            }
        }
        return deleteExitoso;
    }

    /**
     *
     * @return
     */
    public boolean cambiarImagen() {
        HttpSession session = WebContextFactory.get().getSession();
        DataBaseConnection dbcon = null;
        Connection conexion = null;
        boolean cambiarImagen = false;
        DatosUsuarioDTO datos = new DatosUsuarioDTO();
        try {
            dbcon = DataBaseConnection.getInstance();
            conexion = dbcon.getConnection(ContextDataResourceNames.MYSQL_COOMULPINORT_JDBC);
            conexion.setAutoCommit(false);
            datos = (DatosUsuarioDTO) session.getAttribute("datosUsuario");
            datos.setImagenPerfil((String) session.getAttribute("img"));
            cambiarImagen = new UsuarioDAO().cambiarImagen(conexion, datos);
            if (cambiarImagen != false) {
                conexion.commit();
                cambiarImagen = true;
            } else {
                conexion.rollback();
                cambiarImagen = false;
            }
        } catch (Exception e) {
            LoggerMessage.getInstancia().loggerMessageException(e);
        } finally {
            try {
                if (conexion != null && !conexion.isClosed()) {
                    conexion.close();
                    conexion = null;
                }
            } catch (Exception e) {
                LoggerMessage.getInstancia().loggerMessageException(e);
            }
        }
        return cambiarImagen;
    }

    /**
     *
     * @param datosUsuario1
     * @param datosUsuarioSeguridad
     * @return
     */
    public RegistroDTO editarUsuario(UsuarioDTO datosUsuario1, UsuarioSeguridadDTO datosUsuarioSeguridad) {

        HttpSession session = WebContextFactory.get().getSession();
        DatosUsuarioDTO datosUsuario = (DatosUsuarioDTO) session.getAttribute("datosUsuario");
        DataBaseConnection dbcon = null;
        Connection conexion = null;
        boolean editarUsuario = false;
        boolean editarNombreUsuario = false;
        RegistroDTO registroExitoso = null;

        try {
            dbcon = DataBaseConnection.getInstance();
            conexion = dbcon.getConnection(ContextDataResourceNames.MYSQL_COOMULPINORT_JDBC);
            conexion.setAutoCommit(false);
            registroExitoso = new RegistroDTO();
            String fecha = Formato.formatoFecha(datosUsuario1.getFechaNacimiento());
            datosUsuario1.setFechaNacimiento(fecha);
            System.out.println("mediador ::: " + datosUsuario1.toStringJson() + " ::: " + datosUsuarioSeguridad.toStringJson());

            if (!"".equals(datosUsuario1.getId())) {
                editarUsuario = new UsuarioDAO().editarUsuario(conexion, datosUsuario1);
                editarNombreUsuario = new UsuarioSeguridadDAO().editarNombreUsuarioSeguridad(conexion, datosUsuario1, datosUsuarioSeguridad);
            }

            if (editarUsuario != false) {
                conexion.commit();
                registroExitoso.setCondicion(true);
                registroExitoso.setMensaje("Registro Exitoso");
            } else {
                conexion.rollback();
                registroExitoso = null;
            }
            conexion.close();
            conexion = null;

        } catch (Exception e) {
            LoggerMessage.getInstancia().loggerMessageException(e);
        } finally {
            try {
                if (conexion != null && !conexion.isClosed()) {
                    conexion.close();
                    conexion = null;
                }
            } catch (Exception e) {
                LoggerMessage.getInstancia().loggerMessageException(e);
            }
        }
        return registroExitoso;
    }

    /**
     * @param idUsuario
     * @return
     */
    public boolean activarEstadoUsuario(String idUsuario) {
        DataBaseConnection dbcon = null;
        Connection conexion = null;
        boolean registroExitoso = false;
        try {
            dbcon = DataBaseConnection.getInstance();
            conexion = dbcon.getConnection(ContextDataResourceNames.MYSQL_COOMULPINORT_JDBC);
            registroExitoso = new UsuarioDAO().activarEstadoUsuario(conexion, idUsuario, Constantes.ESTADO_ACTIVO);
        } catch (Exception e) {
            LoggerMessage.getInstancia().loggerMessageException(e);
        } finally {
            try {
                if (conexion != null && !conexion.isClosed()) {
                    conexion.close();
                    conexion = null;
                }
            } catch (Exception e) {
                LoggerMessage.getInstancia().loggerMessageException(e);
            }
        }
        return registroExitoso;
    }

    /**
     * @param idUsuario
     * @return
     */
    public boolean inactivarEstadoUsuario(String idUsuario) {
        DataBaseConnection dbcon = null;
        Connection conexion = null;
        boolean registroExitoso = false;
        try {
            dbcon = DataBaseConnection.getInstance();
            conexion = dbcon.getConnection(ContextDataResourceNames.MYSQL_COOMULPINORT_JDBC);
            registroExitoso = new UsuarioDAO().inactivarEstadoUsuario(conexion, idUsuario, Constantes.ESTADO_INACTIVO);
        } catch (Exception e) {
            LoggerMessage.getInstancia().loggerMessageException(e);
        } finally {
            try {
                if (conexion != null && !conexion.isClosed()) {
                    conexion.close();
                    conexion = null;
                }
            } catch (Exception e) {
                LoggerMessage.getInstancia().loggerMessageException(e);
            }
        }
        return registroExitoso;
    }

    /**
     *
     * @return
     */
    public ArrayList<TipoDocumentoDTO> listarTipoDocumento() {
        DataBaseConnection dbcon = null;
        Connection conexion = null;
        ArrayList<TipoDocumentoDTO> listado = null;
        try {
            dbcon = DataBaseConnection.getInstance();
            conexion = dbcon.getConnection(ContextDataResourceNames.MYSQL_COOMULPINORT_JDBC);
            listado = new TipoDocumentoDAO().listarTipoDocumento(conexion, Constantes.ESTADO_ACTIVO);
            conexion.close();
            conexion = null;
        } catch (Exception e) {
            LoggerMessage.getInstancia().loggerMessageException(e);
        } finally {
            try {
                if (conexion != null && !conexion.isClosed()) {
                    conexion.close();
                    conexion = null;
                }
                if (listado != null && listado.isEmpty()) {
                    listado = null;
                }
            } catch (Exception e) {
                LoggerMessage.getInstancia().loggerMessageException(e);
            }
        }
        return listado;
    }

    /**
     *
     * @param datosTipoDocumento
     * @return
     */
    public boolean registrarTipoDocumento(TipoDocumentoDTO datosTipoDocumento) {
        HttpSession session = WebContextFactory.get().getSession();
        DatosUsuarioDTO datosUsuario = (DatosUsuarioDTO) session.getAttribute("datosUsuario");

        DataBaseConnection dbcon = null;
        Connection conexion = null;
        boolean registroExitoso = false;

        try {
            dbcon = DataBaseConnection.getInstance();
            conexion = dbcon.getConnection(ContextDataResourceNames.MYSQL_COOMULPINORT_JDBC);
            conexion.setAutoCommit(false);
            registroExitoso = new TipoDocumentoDAO().registrarTipoDocumento(conexion, datosTipoDocumento, datosUsuario.getUsuario());
        } catch (Exception e) {
            LoggerMessage.getInstancia().loggerMessageException(e);
        } finally {
            try {
                if (conexion != null && !conexion.isClosed()) {
                    conexion.close();
                    conexion = null;
                }
            } catch (Exception e) {
                LoggerMessage.getInstancia().loggerMessageException(e);
            }
        }

        return registroExitoso;
    }

    /**
     * @param datosTipoDocumento
     * @return
     */
    public boolean actualizarTipoDocumento(TipoDocumentoDTO datosTipoDocumento) {
        HttpSession session = WebContextFactory.get().getSession();
        DatosUsuarioDTO datosUsuario = (DatosUsuarioDTO) session.getAttribute("datosUsuario");

        DataBaseConnection dbcon = null;
        Connection conexion = null;
        boolean registroExitoso = false;

        try {
            dbcon = DataBaseConnection.getInstance();
            conexion = dbcon.getConnection(ContextDataResourceNames.MYSQL_COOMULPINORT_JDBC);
            conexion.setAutoCommit(false);
            registroExitoso = new TipoDocumentoDAO().actualizarTipoDocumento(conexion, datosTipoDocumento, datosUsuario.getUsuario());

        } catch (Exception e) {
            LoggerMessage.getInstancia().loggerMessageException(e);
        } finally {
            try {
                if (conexion != null && !conexion.isClosed()) {
                    conexion.close();
                    conexion = null;
                }
            } catch (Exception e) {
                LoggerMessage.getInstancia().loggerMessageException(e);
            }
        }
        return registroExitoso;
    }

    /**
     *
     * @param id
     * @return
     */
    public boolean eliminarTipoDocumento(String id) {

        DataBaseConnection dbcon = null;
        Connection conexion = null;
        boolean deleteExitoso = false;

        try {

            dbcon = DataBaseConnection.getInstance();
            conexion = dbcon.getConnection(ContextDataResourceNames.MYSQL_COOMULPINORT_JDBC);
            deleteExitoso = new TipoDocumentoDAO().eliminarTipoDocumento(conexion, id, Constantes.ESTADO_ACTIVO);

            conexion.close();
            conexion = null;

        } catch (Exception e) {
            LoggerMessage.getInstancia().loggerMessageException(e);
            deleteExitoso = false;
        } finally {
            try {
                if (conexion != null && !conexion.isClosed()) {
                    conexion.close();
                    conexion = null;
                }
            } catch (SQLException e) {
                LoggerMessage.getInstancia().loggerMessageException(e);
                deleteExitoso = false;
            }
        }
        return deleteExitoso;
    }

    /**
     *
     * @return
     */
    public ArrayList<TipoDocumentoDTO> listarTodosLosTipoDocumento() {
        DataBaseConnection dbcon = null;
        Connection conexion = null;
        ArrayList<TipoDocumentoDTO> listado = null;
        try {
            dbcon = DataBaseConnection.getInstance();
            conexion = dbcon.getConnection(ContextDataResourceNames.MYSQL_COOMULPINORT_JDBC);
            listado = new TipoDocumentoDAO().listarTodosLosTipoDocumento(conexion);
            conexion.close();
            conexion = null;
        } catch (Exception e) {
            LoggerMessage.getInstancia().loggerMessageException(e);
        } finally {
            try {
                if (conexion != null && !conexion.isClosed()) {
                    conexion.close();
                    conexion = null;
                }
                if (listado != null && listado.isEmpty()) {
                    listado = null;
                }
            } catch (Exception e) {
                LoggerMessage.getInstancia().loggerMessageException(e);
            }
        }
        return listado;
    }

    /**
     *
     * @return
     */
    public ArrayList<TipoUsuarioDTO> listarCargos() {
        DataBaseConnection dbcon = null;
        Connection conexion = null;
        ArrayList<TipoUsuarioDTO> listado = null;
        try {
            dbcon = DataBaseConnection.getInstance();
            conexion = dbcon.getConnection(ContextDataResourceNames.MYSQL_COOMULPINORT_JDBC);
            listado = new TipoUsuarioDAO().listarCargos(conexion, Constantes.ESTADO_ACTIVO);
            conexion.close();
            conexion = null;
        } catch (Exception e) {
            LoggerMessage.getInstancia().loggerMessageException(e);
        } finally {
            try {
                if (conexion != null && !conexion.isClosed()) {
                    conexion.close();
                    conexion = null;
                }
                if (listado != null && listado.isEmpty()) {
                    listado = null;
                }
            } catch (SQLException e) {
                LoggerMessage.getInstancia().loggerMessageException(e);
            }
        }
        return listado;
    }

    /**
     *
     * @param datosCargo
     * @return
     */
    public boolean registrarCargo(TipoUsuarioDTO datosCargo) {
        HttpSession session = WebContextFactory.get().getSession();
        DatosUsuarioDTO datosUsuario = (DatosUsuarioDTO) session.getAttribute("datosUsuario");

        DataBaseConnection dbcon = null;
        Connection conexion = null;
        boolean registroExitoso = false;

        try {
            dbcon = DataBaseConnection.getInstance();
            conexion = dbcon.getConnection(ContextDataResourceNames.MYSQL_COOMULPINORT_JDBC);
            conexion.setAutoCommit(false);
            registroExitoso = new TipoUsuarioDAO().registrarCargo(conexion, datosCargo, datosUsuario.getUsuario());

        } catch (Exception e) {
            LoggerMessage.getInstancia().loggerMessageException(e);
        } finally {
            try {
                if (conexion != null && !conexion.isClosed()) {
                    conexion.close();
                    conexion = null;
                }
            } catch (Exception e) {
                LoggerMessage.getInstancia().loggerMessageException(e);
            }
        }
        return registroExitoso;
    }

    /**
     * @param datosCargo
     * @return
     */
    public boolean actualizarCargo(TipoUsuarioDTO datosCargo) {
        HttpSession session = WebContextFactory.get().getSession();
        DatosUsuarioDTO datosUsuario = (DatosUsuarioDTO) session.getAttribute("datosUsuario");

        DataBaseConnection dbcon = null;
        Connection conexion = null;
        boolean registroExitoso = false;

        try {
            dbcon = DataBaseConnection.getInstance();
            conexion = dbcon.getConnection(ContextDataResourceNames.MYSQL_COOMULPINORT_JDBC);
            conexion.setAutoCommit(false);
            registroExitoso = new TipoUsuarioDAO().actualizarCargo(conexion, datosCargo, datosUsuario.getUsuario());

        } catch (Exception e) {
            LoggerMessage.getInstancia().loggerMessageException(e);
        } finally {
            try {
                if (conexion != null && !conexion.isClosed()) {
                    conexion.close();
                    conexion = null;
                }
            } catch (Exception e) {
                LoggerMessage.getInstancia().loggerMessageException(e);
            }
        }
        return registroExitoso;
    }

    /**
     *
     * @param id
     * @return
     */
    public boolean eliminarCargo(String id) {

        DataBaseConnection dbcon = null;
        Connection conexion = null;
        boolean deleteExitoso = false;

        try {

            dbcon = DataBaseConnection.getInstance();
            conexion = dbcon.getConnection(ContextDataResourceNames.MYSQL_COOMULPINORT_JDBC);
            deleteExitoso = new TipoUsuarioDAO().eliminarCargo(conexion, id, Constantes.ESTADO_INACTIVO);

            conexion.close();
            conexion = null;

        } catch (Exception e) {
            LoggerMessage.getInstancia().loggerMessageException(e);
            deleteExitoso = false;
        } finally {
            try {
                if (conexion != null && !conexion.isClosed()) {
                    conexion.close();
                    conexion = null;
                }
            } catch (SQLException e) {
                LoggerMessage.getInstancia().loggerMessageException(e);
                deleteExitoso = false;
            }
        }
        return deleteExitoso;
    }

    /**
     *
     * @return
     */
    public ArrayList<TipoUsuarioDTO> listarTodosLosCargos() {
        DataBaseConnection dbcon = null;
        Connection conexion = null;
        ArrayList<TipoUsuarioDTO> listado = null;
        try {
            dbcon = DataBaseConnection.getInstance();
            conexion = dbcon.getConnection(ContextDataResourceNames.MYSQL_COOMULPINORT_JDBC);
            listado = new TipoUsuarioDAO().listarTodosLosCargos(conexion);
            conexion.close();
            conexion = null;
        } catch (Exception e) {
            LoggerMessage.getInstancia().loggerMessageException(e);
        } finally {
            try {
                if (conexion != null && !conexion.isClosed()) {
                    conexion.close();
                    conexion = null;
                }
                if (listado != null && listado.isEmpty()) {
                    listado = null;
                }
            } catch (SQLException e) {
                LoggerMessage.getInstancia().loggerMessageException(e);
            }
        }
        return listado;
    }

    /**
     *
     * @param documento
     * @return
     */
    public DatosUsuarioDTO recuperarContrasenia(String documento) {

        DataBaseConnection dbcon = null;
        Connection conexion = null;
        DatosUsuarioDTO datosUsuario = null;
        String nuevaContrasenia = Generales.EMPTYSTRING;

        try {

            dbcon = DataBaseConnection.getInstance();
            conexion = dbcon.getConnection(ContextDataResourceNames.MYSQL_COOMULPINORT_JDBC);

            datosUsuario = new DatosUsuarioDAO().recuperarContrasenia(conexion, documento);

            if (datosUsuario == null) {
                throw new Exception("no se encotraron datos con con esa cedula");
            }

            String usauId = datosUsuario.getIdUsuario();

            nuevaContrasenia = UUID.randomUUID().toString().substring(0, 6);

            if (!new UsuarioSeguridadDAO().generarContrasenia(conexion, usauId, nuevaContrasenia)) {
                throw new Exception("no se actualiza la coontrasaÃ±a");
            }

            datosUsuario.setClave(nuevaContrasenia);

            conexion.close();
            conexion = null;
        } catch (Exception e) {
            LoggerMessage.getInstancia().loggerMessageException(e);
        } finally {
            try {
                if (conexion != null && !conexion.isClosed()) {
                    conexion.close();
                    conexion = null;
                }
            } catch (Exception e) {
                LoggerMessage.getInstancia().loggerMessageException(e);
            }
        }

        return datosUsuario;
    }

    /**
     *
     * @return @param id
     */
    public UsuarioDTO consultarUsuarioPorId(String id) {
        DataBaseConnection dbcon = null;
        Connection conexion = null;
        UsuarioDTO datos = null;

        try {
            HttpSession session = WebContextFactory.get().getSession();
            DatosUsuarioDTO datosUsuario = (DatosUsuarioDTO) session.getAttribute("datosUsuario");

            dbcon = DataBaseConnection.getInstance();
            conexion = dbcon.getConnection(ContextDataResourceNames.MYSQL_COOMULPINORT_JDBC);

            datos = new UsuarioDAO().consultarUsuarioPorId(conexion, id);
            String fecha = Formato.formatoFechaMostrar(datos.getFechaNacimiento());
            datos.setFechaNacimiento(fecha);

            conexion.close();
            conexion = null;
        } catch (Exception e) {
            LoggerMessage.getInstancia().loggerMessageException(e);
        } finally {
            try {
                if (conexion != null && !conexion.isClosed()) {
                    conexion.close();
                    conexion = null;
                }
            } catch (Exception e) {
                LoggerMessage.getInstancia().loggerMessageException(e);
            }
        }

        return datos;
    }

    /**
     *
     * @param datosUsuario
     * @return
     */
    public boolean actualizarUsuarioPerfil(UsuarioDTO datosUsuario) {

        DataBaseConnection dbcon = null;
        Connection conexion = null;
        boolean registroExitoso = false;

        try {
            dbcon = DataBaseConnection.getInstance();
            conexion = dbcon.getConnection(ContextDataResourceNames.MYSQL_COOMULPINORT_JDBC);

            HttpSession session = WebContextFactory.get().getSession();
            DatosUsuarioDTO datosUsuarioLogueado = (DatosUsuarioDTO) session.getAttribute("datosUsuario");

            datosUsuario.setIdUsuario(datosUsuarioLogueado.getIdUsuario());

            if (!new UsuarioDAO().actualizarUsuarioPerfil(conexion, datosUsuario)) {
                throw new Exception("Error, no se pudo actualizar el usuario");
            }

            registroExitoso = true;

        } catch (Exception e) {
            LoggerMessage.getInstancia().loggerMessageException(e);
        } finally {
            try {
                if (conexion != null && !conexion.isClosed()) {
                    conexion.close();
                    conexion = null;
                }
            } catch (Exception e) {
                LoggerMessage.getInstancia().loggerMessageException(e);
            }
        }
        return registroExitoso;
    }

    /**
     *
     * @param datosUsuarioSeguridad
     * @return
     */
    public boolean cambiarContrasenia(UsuarioSeguridadDTO datosUsuarioSeguridad) {

        DataBaseConnection dbcon = null;
        Connection conexion = null;

        boolean editarUsuarioSeguridad = false;

        try {

            dbcon = DataBaseConnection.getInstance();
            conexion = dbcon.getConnection(ContextDataResourceNames.MYSQL_COOMULPINORT_JDBC);

            HttpSession session = WebContextFactory.get().getSession();
            DatosUsuarioDTO datosUsuario = (DatosUsuarioDTO) session.getAttribute("datosUsuario");

            datosUsuarioSeguridad.setIdUsuario(datosUsuario.getIdUsuario());

            if (!new UsuarioSeguridadDAO().cambiarContrasenia(conexion, datosUsuarioSeguridad)) {
                throw new Exception("Error, no se pudo actualizar la contraseÃ±a");
            }

            editarUsuarioSeguridad = true;

        } catch (Exception e) {
            LoggerMessage.getInstancia().loggerMessageException(e);
        } finally {
            try {
                if (conexion != null && !conexion.isClosed()) {
                    conexion.close();
                    conexion = null;
                }
            } catch (Exception e) {
                LoggerMessage.getInstancia().loggerMessageException(e);
            }
        }
        return editarUsuarioSeguridad;
    }

    /**
     *
     * @param datosUsuario
     * @return
     */
    public boolean validarDocumento(UsuarioDTO datosUsuario) {

        HttpSession session = WebContextFactory.get().getSession();
        DataBaseConnection dbcon = null;
        Connection conexion = null;

        boolean validarUsuario = false;

        try {

            dbcon = DataBaseConnection.getInstance();
            conexion = dbcon.getConnection(ContextDataResourceNames.MYSQL_COOMULPINORT_JDBC);
            conexion.setAutoCommit(false);

            validarUsuario = new UsuarioDAO().validarDocumento(conexion, datosUsuario);

            if (validarUsuario) {
                conexion.commit();

            } else {
                conexion.rollback();
            }
        } catch (Exception e) {
            LoggerMessage.getInstancia().loggerMessageException(e);
        } finally {
            try {
                if (conexion != null && !conexion.isClosed()) {
                    conexion.close();
                    conexion = null;
                }
            } catch (Exception e) {
                LoggerMessage.getInstancia().loggerMessageException(e);
            }
        }
        return validarUsuario;
    }

    /**
     *
     * @param mensaje
     * @return
     */
    public ResponseMensajeDTO enviarMensaje(BodyMensajeDTO mensaje) {

        DataBaseConnection dbcon = null;
        Connection conexion = null;
        boolean registroExitoso = false;
        String urlRequest = Generales.EMPTYSTRING;
        String respuesta = Generales.EMPTYSTRING;
        ResponseMensajeDTO respuestaMensaje = null;

        // Obtener objeto URL
        try {

            dbcon = DataBaseConnection.getInstance();
            conexion = dbcon.getConnection(ContextDataResourceNames.MYSQL_COOMULPINORT_JDBC);
            urlRequest = "https://api.infobip.com/sms/1/text/single";
            URL url = new URL(urlRequest);

            // Establecer conexion a la URL indicada
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Basic  QXZhbnpvOk9jdHVicmUuMjAxNw==");

            conn.setDoInput(true);
            conn.setDoOutput(true);

            logMsg.loggerMessageDebug("Request Body|\n" + mensaje.toStringJson());

            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            wr.writeBytes(mensaje.toStringJson());
            wr.flush();
            wr.close();

            if (conn.getResponseCode() != 200) {
                throw new Exception("Mensaje API Rejection : Error " + conn.getResponseCode() + " : " + conn.getResponseMessage());
            } else {
                registroExitoso = true;
            }

            // =====================================================================================================================================================
            //
            // Obtener respuesta de la conexion
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

            String responseJSON = "";

            // Leer la respuesta obtenida
            while ((respuesta = rd.readLine()) != null) {
                responseJSON += respuesta;
            }
            rd.close();

            // Imprimir la respuesta JSON para DEBUG
            logMsg.loggerMessageDebug("|Response|Mensaje API|\n" + new JSONObject(responseJSON).toString(3));
            responseJSON = new JSONObject(responseJSON).toString(3);
            respuestaMensaje = new ObjectMapper().readValue(responseJSON, ResponseMensajeDTO.class);

        } catch (Exception e) {
            LoggerMessage.getInstancia().loggerMessageException(e);
        } finally {
            try {
                if (conexion != null && !conexion.isClosed()) {
                    conexion.close();
                    conexion = null;
                }
            } catch (Exception e) {
                LoggerMessage.getInstancia().loggerMessageException(e);
            }
        }
        return respuestaMensaje;
    }

    /**
     *
     * @param mensaje
     * @return
     */
    public ResponseMensajeDTO enviarCodigo(BodyMensajeDTO mensaje) {

        DataBaseConnection dbcon = null;
        Connection conexion = null;
        ResponseMensajeDTO respuesta = null;
        try {

            dbcon = DataBaseConnection.getInstance();
            conexion = dbcon.getConnection(ContextDataResourceNames.MYSQL_COOMULPINORT_JDBC);

            if (true) {
                mensaje.setFrom("InfoSMS");
                mensaje.setTo("57" + mensaje.getTo());
                mensaje.setKeyword("verificacion");
                mensaje.setText("El reporte diario del proyecto: " + mensaje.getText() + "no ha sido enviado.");

                if (Constantes.ENVIO_MENSAJE) {
                    ResponseMensajeDTO responseMensaje = enviarMensaje(mensaje);
                    if (!responseMensaje.getMessages().get(0).getStatus().getGroupName().equals(Constantes.RESPONSE_MENSAJE_ENVIADO)) {
                        logMsg.loggerMessageDebug(" | false");
                        logMsg.loggerMessageDebug(" | hubo un error enviando mensaje");
                    } else {
                        logMsg.loggerMessageDebug(" | true");
                        logMsg.loggerMessageDebug(" |  Se envio  mensaje");
                    }
                } else {
                    logMsg.loggerMessageDebug(" | true");
                    logMsg.loggerMessageDebug(" |  Se envio  mensaje");
                }
            } else {
                //datosRespuesta.setResult("false");
                // datosRespuesta.setMensaje("No se encontro codigo de verificacion en el usuario: " + datosUsuario.toStringJson());
            }

        } catch (Exception e) {
            LoggerMessage.getInstancia().loggerMessageException(e);
        } finally {
            try {
                if (conexion != null && !conexion.isClosed()) {
                    conexion.close();
                    conexion = null;
                }
            } catch (Exception e) {
                LoggerMessage.getInstancia().loggerMessageException(e);
            }
        }
        return respuesta;
    }
                    
}
