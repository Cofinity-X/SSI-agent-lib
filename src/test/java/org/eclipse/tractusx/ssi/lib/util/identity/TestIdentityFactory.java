/********************************************************************************
 * Copyright (c) 2021,2023 Contributors to the Eclipse Foundation
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
 ********************************************************************************/

package org.eclipse.tractusx.ssi.lib.util.identity;

import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.OctetKeyPair;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.util.Base64URL;
import java.io.IOException;
import java.net.URI;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.util.List;
import java.util.UUID;
import org.eclipse.tractusx.ssi.lib.crypt.IKeyGenerator;
import org.eclipse.tractusx.ssi.lib.crypt.IPrivateKey;
import org.eclipse.tractusx.ssi.lib.crypt.IPublicKey;
import org.eclipse.tractusx.ssi.lib.crypt.KeyPair;
import org.eclipse.tractusx.ssi.lib.crypt.ec.ECPrivateKeyWrapper;
import org.eclipse.tractusx.ssi.lib.crypt.ec.ECPublicKeyWrapper;
import org.eclipse.tractusx.ssi.lib.crypt.rsa.RSAPrivateKeyWrapper;
import org.eclipse.tractusx.ssi.lib.crypt.rsa.RSAPublicKeyWrapper;
import org.eclipse.tractusx.ssi.lib.crypt.x21559.x21559Generator;
import org.eclipse.tractusx.ssi.lib.exception.KeyGenerationException;
import org.eclipse.tractusx.ssi.lib.model.MultibaseString;
import org.eclipse.tractusx.ssi.lib.model.base.MultibaseFactory;
import org.eclipse.tractusx.ssi.lib.model.did.Did;
import org.eclipse.tractusx.ssi.lib.model.did.DidDocument;
import org.eclipse.tractusx.ssi.lib.model.did.DidDocumentBuilder;
import org.eclipse.tractusx.ssi.lib.model.did.Ed25519VerificationMethod;
import org.eclipse.tractusx.ssi.lib.model.did.Ed25519VerificationMethodBuilder;
import org.eclipse.tractusx.ssi.lib.model.did.JWKVerificationMethod;
import org.eclipse.tractusx.ssi.lib.model.did.JWKVerificationMethodBuilder;
import org.testcontainers.shaded.org.bouncycastle.jce.provider.BouncyCastleProvider;

/** The type Test identity factory. */
public class TestIdentityFactory {

  /**
   * New identity with ed 25519 keys test identity.
   *
   * @return the test identity
   * @throws IOException the io exception
   * @throws KeyGenerationException the key generation exception
   */
  public static TestIdentity newIdentityWithED25519Keys()
      throws IOException, KeyGenerationException {

    final Did did = TestDidFactory.createRandom();

    IKeyGenerator keyGenerator = new x21559Generator();
    KeyPair keyPair = keyGenerator.generateKey();
    IPublicKey publicKey = keyPair.getPublicKey();
    IPrivateKey privateKey = keyPair.getPrivateKey();

    OctetKeyPair.Builder builder =
        (new OctetKeyPair.Builder(Curve.Ed25519, Base64URL.encode(publicKey.asByte())))
            .d(Base64URL.encode(privateKey.asByte()))
            .keyID("key-2");

    MultibaseString multibaseString = MultibaseFactory.create(publicKey.asByte());
    final Ed25519VerificationMethodBuilder ed25519VerificationKey2020Builder =
        new Ed25519VerificationMethodBuilder();

    final Ed25519VerificationMethod ed25519VerificationMethod =
        ed25519VerificationKey2020Builder
            .id(URI.create(did + "#key-2"))
            .controller(URI.create(did + "#controller"))
            .publicKeyMultiBase(multibaseString)
            .build();

    final JWKVerificationMethod jwkVerificationMethod =
        new JWKVerificationMethodBuilder().did(did).jwk(builder.build()).build();

    final DidDocumentBuilder didDocumentBuilder = new DidDocumentBuilder();
    final DidDocument didDocument =
        didDocumentBuilder
            .id(did.toUri())
            .verificationMethods(List.of(ed25519VerificationMethod, jwkVerificationMethod))
            .build();

    return new TestIdentity(did, didDocument, publicKey, privateKey);
  }

  public static TestIdentity newIdentityWithRSAKeys() throws NoSuchAlgorithmException {

    final Did did = TestDidFactory.createRandom();

    KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
    java.security.KeyPair keyPair = kpg.generateKeyPair();
    PublicKey publicKey = keyPair.getPublic();
    PrivateKey privateKey = keyPair.getPrivate();

    RSAKey jwk =
        new RSAKey.Builder((RSAPublicKey) publicKey)
            .privateKey((RSAPrivateKey) privateKey)
            .keyID("1")
            .build();

    final JWKVerificationMethod jwkVerificationMethod =
        new JWKVerificationMethodBuilder().did(did).jwk(jwk.toPublicJWK()).build();

    final DidDocumentBuilder didDocumentBuilder = new DidDocumentBuilder();
    final DidDocument didDocument =
        didDocumentBuilder
            .id(did.toUri())
            .verificationMethods(List.of(jwkVerificationMethod))
            .build();

    return new TestIdentity(
        did,
        didDocument,
        new RSAPublicKeyWrapper(publicKey.getEncoded()),
        new RSAPrivateKeyWrapper(privateKey.getEncoded()));
  }

  public static TestIdentity newIdentityWithECKeys(String alg, Curve crv)
      throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {

    final Did did = TestDidFactory.createRandom();

    KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC", new BouncyCastleProvider());
    ECGenParameterSpec ecGenParameterSpec = new ECGenParameterSpec(alg);
    kpg.initialize(ecGenParameterSpec, new SecureRandom());
    java.security.KeyPair keyPair = kpg.generateKeyPair();
    PublicKey publicKey = keyPair.getPublic();
    PrivateKey privateKey = keyPair.getPrivate();

    ECKey jwk =
        new ECKey.Builder(crv, (ECPublicKey) publicKey)
            .privateKey((ECPrivateKey) privateKey)
            .keyID(UUID.randomUUID().toString())
            .build();

    final JWKVerificationMethod jwkVerificationMethod =
        new JWKVerificationMethodBuilder().did(did).jwk(jwk.toPublicJWK()).build();

    final DidDocumentBuilder didDocumentBuilder = new DidDocumentBuilder();
    final DidDocument didDocument =
        didDocumentBuilder
            .id(did.toUri())
            .verificationMethods(List.of(jwkVerificationMethod))
            .build();

    return new TestIdentity(
        did,
        didDocument,
        new ECPublicKeyWrapper(publicKey.getEncoded()),
        new ECPrivateKeyWrapper(privateKey.getEncoded()));
  }
}
