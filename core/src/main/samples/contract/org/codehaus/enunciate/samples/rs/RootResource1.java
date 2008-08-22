/*
 * Copyright 2006-2008 Web Cohesion
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.enunciate.samples.rs;

import javax.ws.rs.*;

/**
 * @author Ryan Heaton
 */
@Path ("root1")
public class RootResource1 {

  @MatrixParam ("field1")
  private String field1;
  @QueryParam ("field2")
  private int field2;
  private Long property1;

  @GET
  public String getOne() {
    return "one";
  }

  @POST
  public void setOne(String one) {
    //no-op
  }

  @PUT
  public void putOne(String one) {

  }

  @DELETE
  public void deleteOne() {

  }

  @HeaderParam("property1")
  public Long getProperty1() {
    return property1;
  }

  public void setProperty1(Long property1) {
    this.property1 = property1;
  }
}
