package ru.sharmana.resources;

import jersey.repackaged.com.google.common.base.Preconditions;
import org.eclipse.jetty.http.HttpScheme;
import org.eclipse.jetty.http.HttpStatus;
import ru.sharmana.beans.Error;
import ru.sharmana.beans.yamoney.InstanceIdResp;
import ru.sharmana.beans.yamoney.ProcessExternalPayment;
import ru.sharmana.beans.yamoney.RequestExternalPayment;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.net.URI;

import static java.lang.String.format;
import static javax.ws.rs.client.Entity.form;
import static javax.ws.rs.core.UriBuilder.fromUri;
import static ru.sharmana.misc.Props.props;

@Path("pay")
public class PayResource {

    public static final String SUCCESS_STATUS = "success";

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response createPayment(@FormParam("to") String to,
                                  @QueryParam("amount") String amount,
                                  @QueryParam("transaction_name") String name) {
        Preconditions.checkNotNull(to);
        Preconditions.checkNotNull(amount);
        Preconditions.checkNotNull(name);

        InstanceIdResp instanceId = ClientBuilder.newClient()
                .target(fromUri(props().getYaMoneyApiTemplate()).build("instance-id"))
                .request().post(form(new Form("client_id", props().getClientId()))).readEntity(InstanceIdResp.class);

        if (instanceId.getStatus().equals(SUCCESS_STATUS)) {

            RequestExternalPayment requestExternalPayment = ClientBuilder.newClient()
                    .target(fromUri(props().getYaMoneyApiTemplate()).build("request-external-payment"))
                    .request()
                    .post(
                            form(
                                    new Form("pattern_id", "p2p")
                                            .param("instance_id", instanceId.getInstanceId())
                                            .param("to", to)
                                            .param("amount_due", amount)
                                            .param("message", "За событие: " + name)
                            )).readEntity(RequestExternalPayment.class);

            if (requestExternalPayment.getStatus().equals(SUCCESS_STATUS)) {
                ProcessExternalPayment processExtPay = ClientBuilder.newClient()
                        .target(fromUri(props().getYaMoneyApiTemplate()).build("process-external-payment"))
                        .request()
                        .post(
                                form(
                                        new Form("request_id", requestExternalPayment.getRequestId())
                                                .param("instance_id", instanceId.getInstanceId())
                                                .param("ext_auth_success_uri", props().getSharmanaAppUriSuccess())
                                                .param("ext_auth_fail_uri", props().getSharmanaAppUriFailed())
                                )).readEntity(ProcessExternalPayment.class);

                if (processExtPay.getStatus().equals("ext_auth_required")) {

                    URI redirect = fromUri(processExtPay.getAcsUri())
                            .queryParam("cps_context_id", processExtPay.getAcsParams().getCpsContextId())
                            .queryParam("paymentType", processExtPay.getAcsParams().getPaymentType()).build();

                    return Response.status(HttpStatus.OK_200)
                            .entity(format("<html><head><meta http-equiv=\"refresh\" content=\"0; url=%s\" /></head></html>", redirect))
                            .type(MediaType.TEXT_HTML)
                            .build();
                } else {
                    return Response.status(HttpStatus.SERVICE_UNAVAILABLE_503).build();
                }

            } else {
                return Response.status(HttpStatus.INTERNAL_SERVER_ERROR_500)
                        .entity(new Error()
                                .withError(format("can't get request-external-payment: %s",
                                        requestExternalPayment.getError()))
                                .withDescription(requestExternalPayment.getErrorDescription())).build();
            }


        } else {
            return Response.status(HttpStatus.INTERNAL_SERVER_ERROR_500)
                    .entity(new Error().withError("can't get instance-id")
                            .withDescription(instanceId.getError())).build();
        }
    }
}
