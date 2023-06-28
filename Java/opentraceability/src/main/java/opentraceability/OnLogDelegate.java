package opentraceability;

import java.util.ArrayList;
import java.util.List;

interface OnLogDelegate {
    void invoke(OTLog log);
}