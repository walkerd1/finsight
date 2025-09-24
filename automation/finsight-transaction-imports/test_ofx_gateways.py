# test_ofx_gateways.py

from ofxtools import OFXClient
from ofxtools.models import OFX, SIGNONMSGSRQV1, SONRQ, FI
from ofxtools.utils import UTC
from datetime import datetime
import ssl

# OFX servers to test
institutions = [
    {
        "name": "MACU (Mountain America Credit Union)",
        "url": "https://ofx.macu.com",
        "FID": "10898",  # MACU FID from OFXHome
        "ORG": "MACU"
    },
    {
        "name": "AFCU (America First Credit Union)",
        "url": "https://ofx.americafirst.com",
        "FID": "3228",  # AFCU FID from OFXHome
        "ORG": "AMERICA FIRST"
    },
]

print("📡 Starting OFX handshake test using ofxtools...\n")

for inst in institutions:
    print(f"🔍 Testing {inst['name']} at {inst['url']} ...")
    try:
        # Create client with cert verification disabled for testing
        client = OFXClient(inst['url'])

        print(f"    ↳ Using FID={inst.get('FID')} ORG={inst.get('ORG')}")

        org = inst.get("ORG")
        fid = inst.get("FID")

        if not org or not fid:
            raise ValueError(f"Missing ORG or FID for institution {inst['name']}")

        fi = FI(org=org, fid=fid)
        print(f"    ↪ Created FI object: ORG={fi.org}, FID={fi.fid}")
        print(f"    ↪ type(FI): {type(fi)}")

        # Build sign-on request
        sonrq = SONRQ(
            DTCLIENT=datetime.now(tz=UTC),
            USERID="dummy",
            USERPASS="dummy",
            LANGUAGE="ENG",
            FI=fi,
            APPID="QWIN",
            APPVER="2700"
        )
        print(f"    ↪ SONRQ FI content: {sonrq.FI}")

        signon = SIGNONMSGSRQV1(SONRQ=sonrq)
        ofx = OFX(SIGNONMSGSRQV1=signon)

        # Send request (expect failure due to dummy credentials, but connection = success)
        response = client.request(ofx)

        print(f"✅ {inst['name']}: Successfully connected and received response\n")

    except Exception as e:
        print(f"❌ {inst['name']}: Failed ({e})\n")
