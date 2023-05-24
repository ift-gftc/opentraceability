package models.identifiers


//[DataContract]
//[JsonConverter(typeof(GTINConverter))]
class GTIN /*: IEquatable<GTIN>, IComparable<GTIN>*/{

    internal var _gtinStr: String = ""

    constructor() {
    }

    constructor(gtinStr: String?) {
        TODO("Not yet implemented")
    }

    fun IsGS1GTIN(): Boolean {
        TODO("Not yet implemented")
    }

    fun ToDigitalLinkURL(): String {
        TODO("Not yet implemented")
    }

    companion object {
        fun DetectGTINIssue(gtinStr: String?): String? {
            TODO("Not yet implemented")
        }

        fun TryParse(gtinStr: String?, gtin: GTIN?, error: String?): Boolean {
            TODO("Not yet implemented")
        }

        fun IsGTIN(gtinStr: String): Boolean {
            TODO("Not yet implemented")
        }
    }


    fun Clone(): Object {
        TODO("Not yet implemented")
    }

/*
        #region Overrides

        public static bool operator ==(GTIN? obj1, GTIN? obj2)
        {
            try
            {
                if (Object.ReferenceEquals(null, obj1) && Object.ReferenceEquals(null, obj2))
                {
                    return true;
                }

                if (!Object.ReferenceEquals(null, obj1) && Object.ReferenceEquals(null, obj2))
                {
                    return false;
                }

                if (Object.ReferenceEquals(null, obj1) && !Object.ReferenceEquals(null, obj2))
                {
                    return false;
                }

                return obj1?.Equals(obj2) ?? false;
            }
            catch (Exception Ex)
            {
                OTLogger.Error(Ex);
                throw;
            }
        }

        public static bool operator !=(GTIN? obj1, GTIN? obj2)
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

                return !obj1?.Equals(obj2) ?? false;
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

                return this.IsEquals((GTIN)obj);
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
                return this._gtinStr?.ToLower() ?? string.Empty;
            }
            catch (Exception Ex)
            {
                OTLogger.Error(Ex);
                throw;
            }
        }

        #endregion Overrides

        #region IEquatable<GTIN>

        public bool Equals(GTIN? gtin)
        {
            try
            {
                if (Object.ReferenceEquals(null, gtin))
                {
                    return false;
                }

                if (Object.ReferenceEquals(this, gtin))
                {
                    return true;
                }

                return this.IsEquals(gtin);
            }
            catch (Exception Ex)
            {
                OTLogger.Error(Ex);
                throw;
            }
        }

        private bool IsEquals(GTIN? gtin)
        {
            try
            {
                if (Object.ReferenceEquals(null, gtin))
                {
                    return false;
                }

                if (this.ToString().ToLower() == gtin.ToString().ToLower())
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

        #endregion IEquatable<GTIN>

        #region IComparable

        public int CompareTo(GTIN? gtin)
        {
            try
            {
                if (Object.ReferenceEquals(null, gtin))
                {
                    throw new ArgumentNullException(nameof(gtin));
                }

                long myInt64Hash = this.ToString().GetInt64HashCode();
                long otherInt64Hash = gtin.ToString().GetInt64HashCode();

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
