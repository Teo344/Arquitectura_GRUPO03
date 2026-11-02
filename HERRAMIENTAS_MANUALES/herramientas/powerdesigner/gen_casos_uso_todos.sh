#!/usr/bin/env bash
# Regenera los casos de uso de los 4 TG. ATENCIÓN: sobrescribe los .oom/.png/.prj/.sws existentes
# (se pierden los cambios hechos a mano en PowerDesigner).
S="$(cd "$(dirname "$0")" && pwd)"; R="$(cd "$S/../../.." && pwd)"
bash "$S/gen_tg.sh" "$R/TG.1.1. PAO202651_EUREKA_BDDLOCAL_SOAP_JAVA_NRC36884_GR03" SOAP_BDDLOCAL_JAVA CU_General_EurekaBank_SOAP_Java "SISTEMA EUREKABANK – SOAP JAVA CON BDD LOCAL" "SOAP JAVA"
bash "$S/gen_tg.sh" "$R/TG.1.2. PAO202651_EUREKA_BDDLOCAL_SOAP_DOTNET_NRC36884_GR03" SOAP_BDDLOCAL_DOTNET CU_General_EurekaBank_SOAP_DotNet "SISTEMA EUREKABANK – SOAP .NET CON BDD LOCAL" "SOAP .NET"
bash "$S/gen_tg.sh" "$R/TG.1.3. PAO202651_EUREKA_BDDLOCAL_RESTFUL_JAVA_NRC36884_GR03" RESTFUL_BDDLOCAL_JAVA CU_General_EurekaBank_REST_Java "SISTEMA EUREKABANK – REST JAVA CON BDD LOCAL" "REST JAVA"
bash "$S/gen_tg.sh" "$R/TG.1.4. PAO202651_EUREKA_BDDLOCAL_RESTFUL_DOTNET_NRC36884_GR03" RESTFUL_BDDLOCAL_DOTNET CU_General_EurekaBank_REST_DotNet "SISTEMA EUREKABANK – REST .NET CON BDD LOCAL" "REST .NET"
