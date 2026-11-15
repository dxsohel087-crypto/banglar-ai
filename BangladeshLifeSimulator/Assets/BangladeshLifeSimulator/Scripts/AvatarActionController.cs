using UnityEngine;

namespace BangladeshLifeSimulator
{
    /// <summary>
    /// Connect these public methods to Unity UI buttons.
    /// Animator trigger names are configurable and optional.
    /// </summary>
    public sealed class AvatarActionController : MonoBehaviour
    {
        [SerializeField] private Animator animator;
        [SerializeField] private string waveTrigger = "Wave";
        [SerializeField] private string danceTrigger = "Dance";
        [SerializeField] private string interactTrigger = "Interact";

        public void Wave() => Trigger(waveTrigger);
        public void Dance() => Trigger(danceTrigger);
        public void Interact() => Trigger(interactTrigger);

        private void Trigger(string parameterName)
        {
            if (animator == null || string.IsNullOrWhiteSpace(parameterName))
                return;

            if (HasTrigger(animator, parameterName))
                animator.SetTrigger(parameterName);
        }

        private static bool HasTrigger(Animator targetAnimator, string parameterName)
        {
            foreach (AnimatorControllerParameter parameter in targetAnimator.parameters)
            {
                if (parameter.type == AnimatorControllerParameterType.Trigger &&
                    parameter.name == parameterName)
                    return true;
            }

            return false;
        }
    }
}
