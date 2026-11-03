<%-- Componentes.chipEstado: ACTIVO en verde con check; cualquier otro estado en ámbar con advertencia. --%>
<%@ tag pageEncoding="UTF-8" body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="estado" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:chip texto="${empty estado ? 'SIN ESTADO' : estado}" variante="${estado == 'ACTIVO' ? 'exito' : 'advertencia'}"
        icono="${estado == 'ACTIVO' ? 'EXITO' : 'ADVERTENCIA'}"/>
