/*
 * ContextDataResourceNames.java
 *
 * Proyecto: Coomulpinort Puntos
 * Cliente:  Coomulpinort
 * Copyright 2020 by Ing. Carlos Cañizares
 * All rights reserved
 */

package co.IngCarlos.Coomulpinort.mvc.servlet;

import co.IngCarlos.Coomulpinort.common.util.Generales;
import co.IngCarlos.Coomulpinort.common.util.LoggerMessage;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author carlos
 */
@WebServlet(name = "ServletImagenGestion", urlPatterns = {"/ServletImagenGestion"})
public class ServletImagenGestion extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //response.setContentType("text/html;charset=UTF-8");
        response.setContentType("image/jpeg");
        
        try {

            String pathToWeb = Generales.EMPTYSTRING;

            if (System.getProperty("os.name").toLowerCase().startsWith("window")) {
                pathToWeb = (String) new InitialContext().lookup("java:comp/env/ruta_imagen_gestion_windows");
            } else if (System.getProperty("os.name").toLowerCase().startsWith("linux")) {
                pathToWeb = (String) new InitialContext().lookup("java:comp/env/ruta_imagen_gestion_linux");
            }

            String imagen = request.getParameter("imagen");
            System.out.println("ddddd  "+ imagen);

            InputStream is = null;

            try {
                is = new BufferedInputStream(new FileInputStream(pathToWeb + imagen));
                System.out.println(">> "+pathToWeb + imagen);
            } catch (Exception e) {
                is = new BufferedInputStream(new FileInputStream(pathToWeb + "logo.png"));
            }

            IOUtils.copy(is, response.getOutputStream());            

        } catch (Exception e) {
            LoggerMessage.getInstancia().loggerMessageException(e);
        } finally {
        }
        
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
