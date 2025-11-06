// DTO/ConsultarCitasRequest.java
package com.benefactor.agendaCitas.DTO;

public class ConsultarCitasRequest {
    private String documento;
    private String celular;

    // Constructores
    public ConsultarCitasRequest() {}

    public ConsultarCitasRequest(String documento, String celular) {
        this.documento = documento;
        this.celular = celular;
    }

    // Getters y Setters
    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }

    public String getCelular() { return celular; }
    public void setCelular(String celular) { this.celular = celular; }
}