package opentraceability.models.identifiers


//[DataContract]
//[JsonConverter(typeof(GLNConverter))]
class GLN /*: IEquatable<GLN>, IComparable<GLN>*/ {

    internal var _glnStr: String = ""



    constructor() {
    }

    constructor(glnStr: String) {
        TODO("Not yet implemented")
        /*
        try
        {
            string error = GLN.DetectGLNIssue(glnStr);
            if (!string.IsNullOrWhiteSpace(error))
            {
                throw new Exception($"The GLN {glnStr} is invalid. {error}");
            }
            this._glnStr = glnStr;
        }
        catch (Exception Ex)
        {
            OTLogger.Error(Ex);
            throw;
        }
        */
    }



    companion object {
    }

    fun IsGS1PGLN(): Boolean {
        TODO("Not yet implemented")
        //return (_glnStr.Contains(":id:sgln:"))
    }

    fun ToDigitalLinkURL(): String {
        TODO("Not yet implemented")
    }

    fun IsGLN(): Boolean {
        TODO("Not yet implemented")
    }

    fun DetectGLNIssue(glnStr: String): String? {
        TODO("Not yet implemented")
    }

    fun TryParse(glnStr: String, gln: GLN?, error: String?): Boolean {
        TODO("Not yet implemented")
    }

    fun Clone(): Object {
        TODO("Not yet implemented")
    }

    /*
        #region Overrides

        public static bool operator ==(GLN? obj1, GLN? obj2)
        {
            try
            {
                if (Object.ReferenceEquals(null, obj1) && Object.ReferenceEquals(null, obj2))
                {
                    return true;
                }

                if (Object.ReferenceEquals(null, obj1) && Object.ReferenceEquals(null, obj2))
                {
                    return false;
                }

                if (Object.ReferenceEquals(null, obj1) && !Object.ReferenceEquals(null, obj2))
                {
                    return false;
                }

#pragma warning disable CS8602 // Dereference of a possibly null reference.
                return obj1.Equals(obj2);
#pragma warning restore CS8602 // Dereference of a possibly null reference.
            }
            catch (Exception Ex)
            {
                OTLogger.Error(Ex);
                throw;
            }
        }

        public static bool operator !=(GLN? obj1, GLN? obj2)
        {
            try
            {
                if (Object.ReferenceEquals(null, obj1) && Object.ReferenceEquals(null, obj2))
                {
                    return false;
                }

                if (!Object.ReferenceEquals(null, obj1) && Object.ReferenceEquals(null, obj2))
                {
                    return true;
                }

                if (Object.ReferenceEquals(null, obj1) && !Object.ReferenceEquals(null, obj2))
                {
                    return true;
                }

                if (obj1 == null)
                {
                    return true;
                }

                return !obj1.Equals(obj2);
            }
            catch (Exception Ex)
            {
                OTLogger.Error(Ex);
                throw;
            }
        }

        public override bool Equals(object? obj)
        {
            try
            {
                if (Object.ReferenceEquals(null, obj))
                {
                    return false;
                }

                if (Object.ReferenceEquals(this, obj))
                {
                    return true;
                }

                if (obj.GetType() != this.GetType())
                {
                    return false;
                }

                return this.IsEquals((GLN)obj);
            }
            catch (Exception Ex)
            {
                OTLogger.Error(Ex);
                throw;
            }
        }

        public override int GetHashCode()
        {
            try
            {
                int hash = this.ToString().GetInt32HashCode();
                return hash;
            }
            catch (Exception Ex)
            {
                OTLogger.Error(Ex);
                throw;
            }
        }

        public override string ToString()
        {
            try
            {
                return _glnStr.ToLower();
            }
            catch (Exception Ex)
            {
                OTLogger.Error(Ex);
                throw;
            }
        }

        #endregion Overrides

        #region IEquatable<GLN>

        public bool Equals(GLN? gln)
        {
            try
            {
                if (Object.ReferenceEquals(null, gln))
                {
                    return false;
                }

                if (Object.ReferenceEquals(this, gln))
                {
                    return true;
                }

                if (gln is null)
                {
                    return false;
                }
                else
                {
                    return this.ToString() == gln.ToString();
                }
            }
            catch (Exception Ex)
            {
                OTLogger.Error(Ex);
                throw;
            }
        }

        private bool IsEquals(GLN gln)
        {
            try
            {
                if (Object.ReferenceEquals(null, gln))
                {
                    return false;
                }

                if (this.ToString().ToLower() == gln.ToString().ToLower())
                {
                    return true;
                }
                else
                {
                    return false;
                }
            }
            catch (Exception Ex)
            {
                OTLogger.Error(Ex);
                throw;
            }
        }

        #endregion IEquatable<GLN>

        #region IComparable

        public int CompareTo(GLN? gln)
        {
            try
            {
                if (Object.ReferenceEquals(null, gln))
                {
                    throw new ArgumentNullException(nameof(gln));
                }

                long myInt64Hash = this.ToString().GetInt64HashCode();
                long otherInt64Hash = gln.ToString().GetInt64HashCode();

                if (myInt64Hash > otherInt64Hash) return -1;
                if (myInt64Hash == otherInt64Hash) return 0;
                return 1;
            }
            catch (Exception Ex)
            {
                OTLogger.Error(Ex);
                throw;
            }
        }

        #endregion IComparable
     */

}
