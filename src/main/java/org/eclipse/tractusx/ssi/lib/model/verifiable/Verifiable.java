/*
 * ******************************************************************************
 * Copyright (c) 2021,2024 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * *******************************************************************************
 */

package org.eclipse.tractusx.ssi.lib.model.verifiable;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.commons.lang3.SerializationUtils;
import org.eclipse.tractusx.ssi.lib.exception.did.DidParseException;
import org.eclipse.tractusx.ssi.lib.model.JsonLdObject;
import org.eclipse.tractusx.ssi.lib.model.proof.Proof;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.eclipse.tractusx.ssi.lib.model.verifiable.presentation.VerifiablePresentation;
import org.eclipse.tractusx.ssi.lib.serialization.SerializeUtil;

/** The type Verifiable. */
public abstract class Verifiable extends JsonLdObject {

  /** The constant ID. */
  public static final String ID = "id";

  /** The constant TYPE. */
  public static final String TYPE = "type";

  /** The constant PROOF. */
  public static final String PROOF = "proof";

  /** The verification type */
  private VerifiableType verifableType;

  /** The enum Verifiable type. */
  public enum VerifiableType {
    /** Vc verifiable type. */
    VC,
    /** Vp verifiable type. */
    VP
  }

  /**
   * Instantiates a new Verifiable.
   *
   * @param json the json
   * @param type the type
   */
  protected Verifiable(Map<String, Object> json, VerifiableType type) {
    super(json);
    Objects.requireNonNull(this.getId());
    Objects.requireNonNull(this.getTypes());
    Objects.requireNonNull(type, "Verifable Type should not be null");
    this.verifableType = type;
    this.checkId();
  }

  /**
   * Gets proof.
   *
   * @return the proof
   */
  public Optional<Proof> getProof() {

    final Object subject = this.get(PROOF);

    if (subject == null) {
      return Optional.empty();
    }

    return Optional.of(new Proof((Map<String, Object>) subject));
  }

  /**
   * Gets id.
   *
   * @return the id
   */
  @NonNull
  public URI getId() {
    return SerializeUtil.asURI(this.get(ID));
  }

  /**
   * Gets types.
   *
   * @return the types
   */
  @NonNull
  public List<String> getTypes() {
    return (List<String>) this.get(TYPE);
  }

  /**
   * Gets type.
   *
   * @return the type
   */
  public VerifiableType getType() {
    return verifableType;
  }

  /**
   * There exists an error that prevents quads from being created correctly. as this interferes with
   * the credential signature, this is a security risk see
   * https://github.com/eclipse-tractusx/SSI-agent-lib/issues/4 as workaround we ensure that the
   * credential ID starts with one or more letters followed by a colon
   */
  private void checkId() {
    final String regex = "^[a-zA-Z]+:.*$";
    if (!this.getId().toString().matches(regex)) {
      throw new IllegalArgumentException(
          String.format(
              "Invalid VerifiableCredential. Credential ID must start with one or more letters followed by a colon. This is a temporary mitigation for the following security risk: %s",
              "https://github.com/eclipse-tractusx/SSI-agent-lib/issues/4"));
    }
  }

  /**
   * Deep Clone for Verifiable instance
   *
   * @return new deep copy
   */
  public Verifiable deepClone() {
    return SerializationUtils.clone(this);
  }

  /**
   * To get verifiable object with Proof Configuration attribute
   *
   * @return Verifiable
   */
  @SneakyThrows
  public Verifiable removeProofSignature() {

    // Be careful, this function will return new object
    Proof proof =
        this.getProof().orElseThrow(() -> new DidParseException("no proof found for verification"));

    if (proof != null) {
      var proofConfiguration = proof.toConfiguration();

      // We need to update this object with new Proof
      this.put(PROOF, proofConfiguration);
    }

    if (this.getType() == VerifiableType.VP) {

      VerifiablePresentation vp = (VerifiablePresentation) this;
      List<VerifiableCredential> newVCsWithConfiguration = new ArrayList<>();

      for (Iterator<VerifiableCredential> iterator = vp.getVerifiableCredentials().iterator();
          iterator.hasNext(); ) {

        VerifiableCredential vc = iterator.next();
        proof =
            vc.getProof()
                .orElseThrow(() -> new DidParseException("no proof found for verification"));

        if (proof != null) {
          var proofConfiguration = proof.toConfiguration();

          // We need to update the copy object with new Proof
          vc.put(PROOF, proofConfiguration);
        }
        newVCsWithConfiguration.add(vc);
      }

      vp.remove(VerifiablePresentation.VERIFIABLE_CREDENTIAL);
      vp.put(VerifiablePresentation.VERIFIABLE_CREDENTIAL, newVCsWithConfiguration);
    }

    return this;
  }
}
