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

package org.eclipse.tractusx.ssi.lib.model.proof.jws;

import java.util.Map;
import java.util.Objects;
import org.eclipse.tractusx.ssi.lib.model.proof.Proof;

/**
 * The type of Ed25519Configuration
 *
 * <p>E.g. "proof": {"type": "JsonWebSignature2020", "created": "2019-12-11T03:50:55Z",
 * "proofPurpose": "assertionMethod", "verificationMethod":
 * "https://example.com/issuer/123#ovsDKYBjFemIy8DVhc-w2LSi8CvXMw2AYDzHj04yxkc"}
 */
public class JWSProofConfiguration extends Proof {

  /** The constant JWS_VERIFICATION_KEY_2020. */
  public static final String JWS_VERIFICATION_KEY_2020 = "JsonWebSignature2020";

  /** The constant TIME_FORMAT. */
  public static final String TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

  /** The constant ASSERTION_METHOD. */
  public static final String ASSERTION_METHOD = "assertionMethod";

  /** The constant PROOF_PURPOSE. */
  public static final String PROOF_PURPOSE = "proofPurpose";

  /** The constant CREATED. */
  public static final String CREATED = "created";

  /** The constant VERIFICATION_METHOD. */
  public static final String VERIFICATION_METHOD = "verificationMethod";

  /**
   * Instantiates a new Jws signature 2020.
   *
   * @param json the json
   */
  public JWSProofConfiguration(Map<String, Object> json) {
    super(json);

    if (!JWS_VERIFICATION_KEY_2020.equals(json.get(TYPE))) {
      throw new IllegalArgumentException("Invalid JsonWebSignature2020 Type: " + json);
    }

    try {
      // verify getters
      Objects.requireNonNull(this.getProofPurpose());
      Objects.requireNonNull(this.getVerificationMethod());
      Objects.requireNonNull(this.getCreated());
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid JsonWebSignature2020", e);
    }
  }
}
