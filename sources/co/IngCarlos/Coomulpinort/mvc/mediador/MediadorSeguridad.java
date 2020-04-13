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
import co.IngCarlos.Coomulpinort.mvc.dto.DatosUsuarioDTO;
import co.IngCarlos.Coomulpinort.common.connection.DataBaseConnection;
import co.IngCarlos.Coomulpinort.common.util.LoggerMessage;
import co.IngCarlos.Coomulpinort.mvc.dao.DatosUsuarioDAO;
import co.IngCarlos.Coomulpinort.mvc.dao.FuncionalidadDAO;
import co.IngCarlos.Coomulpinort.mvc.dao.MenuDAO;
import co.IngCarlos.Coomulpinort.mvc.dto.FuncionalidadDTO;
import co.IngCarlos.Coomulpinort.mvc.dto.MenuDTO;
import java.sql.Connection;
import java.util.ArrayList;



/**
 *
 * @author carlos
 */
public class MediadorSeguridad {
    
    /**
     *
     */
    private final static MediadorSeguridad instancia = null;
    private final LoggerMessage logMsg;

    /**
     *
     */
    public MediadorSeguridad() {
        logMsg = LoggerMessage.getInstancia();
    }

    /**
     *
     * @return
     */
    public static synchronized MediadorSeguridad getInstancia() {
        return instancia == null ? new MediadorSeguridad() : instancia;
    }

    /**
     *
     * @param usuario
     * @return
     */
    public DatosUsuarioDTO consultarDatosUsuarioLogueado(String usuario) { 
        DataBaseConnection dbcon = null;
        Connection conexion = null;
        DatosUsuarioDTO datosUsuario = null;
        try {
            System.out.print("llega a mediador");
            dbcon = DataBaseConnection.getInstance();
            conexion = dbcon.getConnection(ContextDataResourceNames.MYSQL_COOMULPINORT_JDBC);
            datosUsuario = new DatosUsuarioDAO().consultarDatosUsuarioLogueado(conexion, usuario);
            System.out.print("datos Usuario" + datosUsuario.toStringJson());
            ArrayList<MenuDTO> datosMenu = new MenuDAO().listarMenusPorUsuario(conexion, datosUsuario.getIdTipoUsuario());
            System.out.print("llega a mediador de consultar menu");
            datosUsuario.setMenu(datosMenu);
            for (int i = 0; i < datosMenu.size(); i++) {
                ArrayList<FuncionalidadDTO> datosFuncionalidades = new FuncionalidadDAO().listarFuncionalidadesPorMenu(conexion, datosMenu.get(i).getId(), datosUsuario.getIdTipoUsuario());
                datosMenu.get(i).setFuncionalidad(datosFuncionalidades);
            }
        } catch (Exception e) {
            logMsg.loggerMessageException(e);
        } finally {
            try {
                if (conexion != null && !conexion.isClosed()) {
                    conexion.close();
                    conexion = null;
                }
            } catch (Exception e) {
                logMsg.loggerMessageException(e);
            }
        }
        return datosUsuario;
    }

    /**
     * -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
     * LOS METODOS APARTIR DE AQUI NO HAN SIDO VALIDADOS
     * -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
     */
    
}
