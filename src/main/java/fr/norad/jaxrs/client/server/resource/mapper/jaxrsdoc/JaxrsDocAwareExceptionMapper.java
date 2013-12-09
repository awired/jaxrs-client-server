/**
 *
 *     Copyright (C) norad.fr
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package fr.norad.jaxrs.client.server.resource.mapper.jaxrsdoc;


import static fr.norad.jaxrs.client.server.resource.mapper.ExceptionMapperUtils.findMediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fr.norad.core.lang.reflect.AnnotationUtils;
import fr.norad.jaxrs.client.server.resource.Error;
import fr.norad.jaxrs.client.server.resource.mapper.ExceptionMapperUtils;
import fr.norad.jaxrs.doc.api.HttpStatus;
import lombok.Data;

@Data
public class JaxrsDocAwareExceptionMapper implements ExceptionMapper<Exception> {

    private Logger log = LoggerFactory.getLogger(getClass());
    private boolean hideException = false;
    private boolean hideMessage = false;
    private boolean logError = true;
    private int defaultExceptionHttpCode = 400;
    private int defaultRuntimeExceptionHttpCode = 500;

    @Override
    public Response toResponse(Exception exception) {
        int httpCode = RuntimeException.class.isAssignableFrom(exception.getClass()) ?
                defaultRuntimeExceptionHttpCode : defaultExceptionHttpCode;
        HttpStatus status = AnnotationUtils.findAnnotation(exception.getClass(), HttpStatus.class);
        if (status != null) {
            httpCode = status.value();
        }

        Error error = buildError(exception);
        if (hideException) {
            error.setException(null);
        }
        if (hideMessage) {
            error.setMessage(null);
        }

        logError(exception);
        return Response.status(httpCode).entity(error).type(findMediaType()).build();
    }

    private void logError(Exception exception) {
        if (logError) {
            if (log.isDebugEnabled()) {
                log.debug("Respond error", exception);
            } else {
                log.info("Respond error : {}", exception.getMessage());
            }
        }
    }

    public Error buildError(Exception exception) {
        return ExceptionMapperUtils.buildError(exception);
    }
}