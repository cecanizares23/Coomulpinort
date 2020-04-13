<%-- 
    Document   : index_ctr
    Created on : 13/04/2020, 05:37:45 PM
    Author     : Ing. Carlos
--%>

<%@ page import="javax.servlet.*"%>
<%@ page import="java.util.*,java.io.*"%>
<%@ page import="co.IngCarlos.Coomulpinort.mvc.dto.*"%>
<%@ page import="co.IngCarlos.Coomulpinort.common.util.*"%>
<jsp:useBean type="co.IngCarlos.Coomulpinort.mvc.fachada.FachadaSeguridad" scope="application" id="fachadaSeguridad"/>

<%
    DatosUsuarioDTO datosUsuario = null;
    try {

        if (!ValidaString.isNullOrEmptyString(request.getRemoteUser())) {

            datosUsuario = fachadaSeguridad.consultarDatosUsuarioLogueado(request.getRemoteUser());
            
             //DatosUsuarioDTO datosUsuario1 = (DatosUsuarioDTO) session.getAttribute("datosUsuario");
             //System.out.println("datosUsuario CTR " + datosUsuario1.toStringJson());

            if (datosUsuario != null && datosUsuario.getEstado().equals("1")) {

                session.setAttribute("datosUsuario", datosUsuario);
//                response.sendRedirect(request.getContextPath() + "/index.jsp");
                request.getRequestDispatcher("/index.jsp").forward(request, response);
            } else {

                request.getRequestDispatcher("/login.jsp?error=Ingreso no autorizado").forward(request, response);
            }
        } else {

            request.getRequestDispatcher("/login.jsp").forward(request, response);
        }
    } catch (Exception e) {
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }
%>
