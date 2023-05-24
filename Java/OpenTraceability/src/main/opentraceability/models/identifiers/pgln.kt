package models.identifiers

import models.identifiers.*

class PGLN {

    internal var _pglnStr: String = ""

    constructor() {

    }

    constructor(pglnStr: String?) {
        TODO("Not yet implemented")
    }

    fun IsGS1PGLN(): Boolean {
        TODO("Not yet implemented")
    }

    fun ToDigitalLinkURL(): String {
        TODO("Not yet implemented")
    }


    companion object {
        fun IsPGLN(pglnStr: String): Boolean {
            TODO("Not yet implemented")
        }

        fun DetectPGLNIssue(pglnStr: String?): String? {
            TODO("Not yet implemented")
        }

        fun TryParse(pglnStr: String?, pgln: PGLN?, error: String?): Boolean {
            TODO("Not yet implemented")
        }
    }



    fun Clone(): Object {
        TODO("Not yet implemented")
    }

    /*
        #region Overrides

        public static bool operator ==(PGLN? obj1, PGLN? obj2)
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

                if (obj1 == null)
                {
                    return false;
                }

                return obj1.Equals(obj2);
            }
            catch (Exception Ex)
            {
                OTLogger.Error(Ex);
                throw;
            }
        }

        public static bool operator !=(PGLN? obj1, PGLN? obj2)
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
                    return false;
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

                return this.IsEquals((PGLN)obj);
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
                return this._pglnStr.ToLower();
            }
            catch (Exception Ex)
            {
                OTLogger.Error(Ex);
                throw;
            }
        }

        #endregion Overrides

        #region IEquatable

        public bool Equals(PGLN? pgln)
        {
            try
            {
                if (Object.ReferenceEquals(null, pgln))
                {
                    return false;
                }

                if (Object.ReferenceEquals(this, pgln))
                {
                    return true;
                }

                return this.IsEquals(pgln);
            }
            catch (Exception Ex)
            {
                OTLogger.Error(Ex);
                throw;
            }
        }

        private bool IsEquals(PGLN? pgln)
        {
            try
            {
                if (pgln == null) throw new ArgumentNullException(nameof(pgln));

                if (this.ToString().ToLower() == pgln.ToString().ToLower())
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

        #endregion IEquatable

        #region IComparable

        public int CompareTo(PGLN? pgln)
        {
            try
            {
                if (pgln == null) throw new ArgumentNullException(nameof(pgln));

                long myInt64Hash = this.ToString().ToLower().GetInt64HashCode();
                long otherInt64Hash = pgln.ToString().ToLower().GetInt64HashCode();

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
